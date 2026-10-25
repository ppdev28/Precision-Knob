package com.sonicdial.app.debug

import android.app.Activity
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import kotlin.math.PI
import kotlin.math.sin

/**
 * Plays a steady 440 Hz tone and walks through global audio-effect settings so you can HEAR whether
 * they change the output. Each step lasts [StepMs]. Set media volume to ~50% first.
 */
class ToneTestActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var label: TextView
    private var track: AudioTrack? = null
    private var player: Thread? = null
    @Volatile private var running = true
    private var loudness: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null

    private class Step(val name: String, val apply: () -> Unit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        label = TextView(this).apply { textSize = 22f; setPadding(48, 160, 48, 48) }
        setContentView(label)
        startTone()

        loudness = runCatching { LoudnessEnhancer(0).also { it.enabled = true } }.onFailure { Log.w(Tag, "LoudnessEnhancer: $it") }.getOrNull()
        equalizer = runCatching { Equalizer(0, 0).also { it.enabled = true } }.onFailure { Log.w(Tag, "Equalizer: $it") }.getOrNull()

        fun gain(mb: Int, label: String) = Step("LoudnessEnhancer $label") {
            equalizer?.let { eq -> for (b in 0 until eq.numberOfBands) eq.setBandLevel(b.toShort(), 0) }
            loudness?.setTargetGain(mb)
        }
        fun eqAll(mb: Int, label: String) = Step("Equalizer $label") {
            loudness?.setTargetGain(0)
            equalizer?.let { eq -> for (b in 0 until eq.numberOfBands) eq.setBandLevel(b.toShort(), mb.toShort()) }
        }
        val steps = listOf(
            gain(0, "0 dB (reference)"), gain(-300, "-3 dB"), gain(-600, "-6 dB"), gain(-1200, "-12 dB"), gain(-2000, "-20 dB"),
            gain(0, "0 dB (reference again)"),
            eqAll(-1500, "all bands -15 dB"), eqAll(0, "flat (reference)"), eqAll(-1500, "all bands -15 dB again"), eqAll(0, "flat"),
        )
        steps.forEachIndexed { i, step ->
            handler.postDelayed({
                label.text = "Step ${i + 1}/${steps.size}\n${step.name}\n\nTone should get quieter at the negative steps and return at the 0 dB / flat ones."
                Log.i(Tag, "STEP ${i + 1}: ${step.name}")
                runCatching { step.apply() }.onFailure { Log.w(Tag, "apply failed: $it") }
            }, i * StepMs)
        }
        handler.postDelayed({ label.text = "Done"; Log.i(Tag, "DONE") }, steps.size * StepMs)
    }

    private fun startTone() {
        val rate = 48_000
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
            .setBufferSizeInBytes(rate * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track = t
        t.play()
        player = Thread {
            val chunk = ShortArray(rate / 10 * 2)
            var phase = 0.0
            while (running) {
                for (i in 0 until chunk.size / 2) {
                    val v = (sin(phase) * 0.3 * Short.MAX_VALUE).toInt().toShort()
                    chunk[2 * i] = v; chunk[2 * i + 1] = v
                    phase += 2 * PI * 440.0 / rate
                }
                t.write(chunk, 0, chunk.size)
            }
        }.also { it.start() }
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        player?.join(500)
        track?.release(); loudness?.release(); equalizer?.release()
        super.onDestroy()
    }

    private companion object {
        const val Tag = "ToneTest"
        const val StepMs = 4_000L
    }
}
