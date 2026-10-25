package com.sonicdial.app.audio

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import com.sonicdial.app.audio.eq.EqFit
import com.sonicdial.app.audio.eq.ParametricFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

/**
 * Global-mix audio effects (audio session 0), the only ones verified to work on the target phone
 * (a listening test showed DynamicsProcessing is rejected there, while these two are audible):
 * - [LoudnessEnhancer] with negative gain: sub-step volume trim (1% volume control) plus the profile preamp.
 * - [Equalizer]: the few hardware bands, fitted to the headphone profile with [EqFit].
 *
 * Global-mix effects are deprecated and device dependent, so [trimAvailable] / [eqAvailable] must be checked.
 * They only live while this process does: [keepAlive] tells the app when to hold a foreground service.
 */
class AudioFxEngine {
    private var loudness: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null
    private var bandRangesHz: List<Pair<Float, Float>> = emptyList()
    private var levelRangeDb: ClosedFloatingPointRange<Float> = -15f..15f

    private var trimDb = 0f
    private var preampDb = 0f
    private var eqEnabled = false
    private var filters: List<ParametricFilter> = emptyList()

    private val _trimAvailable = MutableStateFlow(false)
    val trimAvailable: StateFlow<Boolean> = _trimAvailable
    private val _eqAvailable = MutableStateFlow(false)
    val eqAvailable: StateFlow<Boolean> = _eqAvailable
    private val _keepAlive = MutableStateFlow(false)
    val keepAlive: StateFlow<Boolean> = _keepAlive

    val isTrimAvailable: Boolean get() = _trimAvailable.value

    @Synchronized
    fun start() {
        if (loudness == null) {
            try {
                loudness = LoudnessEnhancer(0)
                _trimAvailable.value = true
            } catch (e: Exception) {
                Log.w(Tag, "LoudnessEnhancer unavailable: fine volume disabled", e)
            }
        }
        if (equalizer == null) {
            try {
                val eq = Equalizer(0, 0)
                val range = eq.bandLevelRange
                levelRangeDb = (range[0] / 100f)..(range[1] / 100f)
                bandRangesHz = (0 until eq.numberOfBands).map { band ->
                    val r = eq.getBandFreqRange(band.toShort())
                    (r[0] / 1000f) to (r[1] / 1000f)
                }
                equalizer = eq
                _eqAvailable.value = true
                Log.i(Tag, "Equalizer bands (Hz): $bandRangesHz, range dB: $levelRangeDb")
            } catch (e: Exception) {
                Log.w(Tag, "Equalizer unavailable: headphone EQ disabled", e)
            }
        }
        applyEq()
        applyGain()
    }

    /** Negative gain (dB) applied on top of the system volume for sub-step precision. */
    @Synchronized
    fun setTrimDb(db: Float) {
        trimDb = db.coerceAtMost(0f)
        applyGain()
    }

    @Synchronized
    fun setEq(enabled: Boolean, preampDb: Float, filters: List<ParametricFilter>) {
        eqEnabled = enabled
        this.preampDb = preampDb
        this.filters = filters
        applyEq()
        applyGain()
    }

    private fun applyEq() {
        val eq = equalizer ?: return
        guarded {
            if (eqEnabled) {
                val gains = EqFit.bandGains(filters, bandRangesHz, levelRangeDb.start, levelRangeDb.endInclusive)
                gains.forEachIndexed { band, db -> eq.setBandLevel(band.toShort(), (db * 100).roundToInt().toShort()) }
            }
            eq.enabled = eqEnabled
        }
        updateKeepAlive()
    }

    private fun applyGain() {
        val le = loudness ?: return
        val totalDb = (trimDb + if (eqEnabled) preampDb else 0f).coerceAtMost(0f)
        guarded {
            le.setTargetGain((totalDb * 100).roundToInt())
            le.enabled = totalDb < -0.01f
        }
        updateKeepAlive()
    }

    private fun updateKeepAlive() {
        _keepAlive.value = eqEnabled || trimDb < -0.01f
    }

    private inline fun guarded(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.w(Tag, "Audio effect call failed", e)
        }
    }

    private companion object {
        const val Tag = "AudioFxEngine"
    }
}
