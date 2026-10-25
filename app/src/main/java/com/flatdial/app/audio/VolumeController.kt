package com.flatdial.app.audio

import android.content.Context
import android.database.ContentObserver
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

/**
 * Snapshot of one stream.
 * - Native streams: [percent] is rounded to the nearest system step.
 * - Fine media volume ([fine] = true): [percent] has 1% resolution = system step + [trimDb] attenuation.
 */
data class StreamVolume(
    val level: Int = 0,
    val min: Int = 0,
    val max: Int = 0,
    val muted: Boolean = false,
    val percent: Int = 0,
    val trimDb: Float = 0f,
    val fine: Boolean = false,
) {
    val available: Boolean get() = max > min
    val steps: Int get() = max - min
}

class VolumeController(context: Context, private val fx: AudioFxEngine) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val percentBeforeMute = mutableMapOf<AudioStreamType, Int>()
    private val tableCache = mutableMapOf<Triple<Int, Int, Int>, List<Float>>()

    private var trimDb = 0f
    private var lastSetLevel: Int? = null
    private var lastSetAt = 0L

    /** Fine (1%) control needs the global audio effect, and only applies to media: the trim affects the whole mix. */
    fun isFine(stream: AudioStreamType) = stream == AudioStreamType.Media && fx.isTrimAvailable

    fun read(stream: AudioStreamType): StreamVolume {
        val id = stream.androidStream
        val min = audioManager.getStreamMinVolume(id)
        val max = audioManager.getStreamMaxVolume(id)
        val level = audioManager.getStreamVolume(id)
        val muted = audioManager.isStreamMute(id) || (level == min && min == 0)
        val base = StreamVolume(level = level, min = min, max = max, muted = muted)
        if (max <= min) return base

        if (!isFine(stream)) {
            trimDb = 0f
            return base.copy(percent = (((level - min) * 100f) / (max - min)).roundToInt().coerceIn(0, 100))
        }
        detectExternalChange(level)
        val table = stepTable(stream, min, max)
        return base.copy(
            percent = VolumeMapper.percentFor(level - min, trimDb, table),
            trimDb = trimDb,
            fine = true,
        )
    }

    /** Sets 0..100%. Media gets 1% resolution; other streams snap to the nearest system step. */
    fun setPercent(stream: AudioStreamType, percent: Int): StreamVolume {
        val current = read(stream)
        if (!current.available) return current
        val p = percent.coerceIn(0, 100)
        if (!current.fine) {
            return setLevel(stream, current.min + (p * current.steps / 100f).roundToInt())
        }
        val target = VolumeMapper.targetFor(p, stepTable(stream, current.min, current.max))
        trimDb = target.trimDb
        fx.setTrimDb(trimDb)
        return setLevel(stream, current.min + target.index)
    }

    /** One step = 1% for media, one system step otherwise. */
    fun step(stream: AudioStreamType, delta: Int): StreamVolume {
        val current = read(stream)
        if (current.fine) return setPercent(stream, current.percent + delta)
        return setLevel(stream, (current.level + delta).coerceIn(current.min, current.max))
    }

    fun toggleMute(stream: AudioStreamType): StreamVolume {
        val current = read(stream)
        if (current.muted) {
            if (audioManager.isStreamMute(stream.androidStream)) {
                audioManager.adjustStreamVolume(stream.androidStream, AudioManager.ADJUST_UNMUTE, 0)
            }
            return setPercent(stream, percentBeforeMute.remove(stream)?.takeIf { it > 0 } ?: 10)
        }
        percentBeforeMute[stream] = current.percent
        return setPercent(stream, 0)
    }

    private fun setLevel(stream: AudioStreamType, level: Int): StreamVolume {
        lastSetLevel = level
        lastSetAt = SystemClock.uptimeMillis()
        try {
            audioManager.setStreamVolume(stream.androidStream, level, 0)
        } catch (_: SecurityException) {
            // Some ROMs refuse volume changes (e.g. Do Not Disturb policy); the state is re-read below.
        }
        return read(stream)
    }

    /** Hardware keys or other apps moved the system step: the old trim no longer applies. */
    private fun detectExternalChange(level: Int) {
        val expected = lastSetLevel
        val settled = SystemClock.uptimeMillis() - lastSetAt > SettleMs
        if (settled && expected != level && trimDb != 0f) {
            trimDb = 0f
            fx.setTrimDb(0f)
        }
        lastSetLevel = level
    }

    private fun stepTable(stream: AudioStreamType, min: Int, max: Int): List<Float> {
        val device = outputDeviceType()
        return tableCache.getOrPut(Triple(stream.androidStream, device, max - min)) {
            val raw = runCatching {
                (min..max).map { audioManager.getStreamVolumeDb(stream.androidStream, it, device) }
            }.getOrNull()
            val table = raw?.let(VolumeMapper::normalize)
            if (table != null && VolumeMapper.isUsable(table)) table else VolumeMapper.linearTable(max - min)
        }
    }

    private fun outputDeviceType(): Int {
        val connected = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.type }.toSet()
        return DevicePriority.firstOrNull { it in connected } ?: AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
    }

    /** Emits whenever any system volume changes (hardware keys, other apps, system UI). */
    fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        appContext.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        awaitClose { appContext.contentResolver.unregisterContentObserver(observer) }
    }

    private companion object {
        const val SettleMs = 600L
        val DevicePriority = listOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
        )
    }
}
