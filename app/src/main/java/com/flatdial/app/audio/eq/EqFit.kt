package com.flatdial.app.audio.eq

import kotlin.math.max
import kotlin.math.min

/**
 * Approximates a parametric filter chain with the few fixed bands of Android's hardware-style Equalizer
 * (typically 5 bands, ±15 dB): each band gets the average response (in dB) over its frequency range.
 */
object EqFit {
    private const val SamplesPerBand = 12
    private const val MinHz = 20f
    private const val MaxHz = 20_000f

    /** [rangesHz] is one (low, high) pair per band; returns one gain per band, clamped to [minDb]..[maxDb]. */
    fun bandGains(
        filters: List<ParametricFilter>,
        rangesHz: List<Pair<Float, Float>>,
        minDb: Float,
        maxDb: Float,
    ): List<Float> = rangesHz.map { (low, high) ->
        val lo = max(low, MinHz)
        val hi = min(high, MaxHz).coerceAtLeast(lo * 1.01f)
        val points = logSpaced(SamplesPerBand, lo, hi)
        val mean = points.map { filters.responseDb(it) }.average().toFloat()
        mean.coerceIn(minDb, maxDb)
    }
}
