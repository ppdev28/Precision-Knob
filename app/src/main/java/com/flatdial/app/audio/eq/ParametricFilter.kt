package com.flatdial.app.audio.eq

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class FilterType { Peak, LowShelf, HighShelf }

/** One biquad band of a parametric EQ (RBJ cookbook formulas, as used by AutoEq / EqualizerAPO). */
data class ParametricFilter(
    val type: FilterType,
    val freqHz: Float,
    val gainDb: Float,
    val q: Float,
) {
    /** Magnitude response in dB at [hz], assuming a 48 kHz sample rate. */
    fun magnitudeDb(hz: Float): Float {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2 * PI * freqHz / SampleRate
        val cosW0 = cos(w0)
        val alpha = sin(w0) / (2 * q)
        val sqrtA2Alpha = 2 * sqrt(a) * alpha

        val b0: Double; val b1: Double; val b2: Double
        val a0: Double; val a1: Double; val a2: Double
        when (type) {
            FilterType.Peak -> {
                b0 = 1 + alpha * a; b1 = -2 * cosW0; b2 = 1 - alpha * a
                a0 = 1 + alpha / a; a1 = -2 * cosW0; a2 = 1 - alpha / a
            }
            FilterType.LowShelf -> {
                b0 = a * ((a + 1) - (a - 1) * cosW0 + sqrtA2Alpha)
                b1 = 2 * a * ((a - 1) - (a + 1) * cosW0)
                b2 = a * ((a + 1) - (a - 1) * cosW0 - sqrtA2Alpha)
                a0 = (a + 1) + (a - 1) * cosW0 + sqrtA2Alpha
                a1 = -2 * ((a - 1) + (a + 1) * cosW0)
                a2 = (a + 1) + (a - 1) * cosW0 - sqrtA2Alpha
            }
            FilterType.HighShelf -> {
                b0 = a * ((a + 1) + (a - 1) * cosW0 + sqrtA2Alpha)
                b1 = -2 * a * ((a - 1) + (a + 1) * cosW0)
                b2 = a * ((a + 1) + (a - 1) * cosW0 - sqrtA2Alpha)
                a0 = (a + 1) - (a - 1) * cosW0 + sqrtA2Alpha
                a1 = 2 * ((a - 1) - (a + 1) * cosW0)
                a2 = (a + 1) - (a - 1) * cosW0 - sqrtA2Alpha
            }
        }
        val w = 2 * PI * hz / SampleRate
        val c1 = cos(w); val s1 = sin(w)
        val c2 = cos(2 * w); val s2 = sin(2 * w)
        // H(z) with z^-1 = e^{-jw}
        val numRe = b0 + b1 * c1 + b2 * c2
        val numIm = -(b1 * s1 + b2 * s2)
        val denRe = a0 + a1 * c1 + a2 * c2
        val denIm = -(a1 * s1 + a2 * s2)
        val mag2 = (numRe * numRe + numIm * numIm) / (denRe * denRe + denIm * denIm)
        return (10 * log10(mag2)).toFloat()
    }

    private companion object {
        const val SampleRate = 48_000.0
    }
}

/** Combined response of a filter chain (without preamp) at [hz]. */
fun List<ParametricFilter>.responseDb(hz: Float): Float = sumOf { it.magnitudeDb(hz).toDouble() }.toFloat()

/** Log-spaced frequencies between [fromHz] and [toHz], inclusive. */
fun logSpaced(count: Int, fromHz: Float = 20f, toHz: Float = 20_000f): List<Float> =
    List(count) { i -> fromHz * (toHz / fromHz).pow(i / (count - 1f)) }
