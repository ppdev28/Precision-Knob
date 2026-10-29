package com.precisevolumedial.app.audio

import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.roundToInt

/**
 * Maps a 0..100% "fine" volume onto (system step, software attenuation).
 *
 * Android only offers ~15 steps, each several dB apart. We read the real dB of every step
 * (`AudioManager.getStreamVolumeDb`), interpolate a target dB for the requested percent, then pick the
 * smallest system step that is at least as loud and trim the difference with a negative gain.
 */
object VolumeMapper {
    data class Target(val index: Int, val trimDb: Float)

    /** [stepDb] holds one dB value per system step, index 0 = lowest step. */
    fun targetFor(percent: Int, stepDb: List<Float>): Target {
        val steps = stepDb.size - 1
        if (steps <= 0 || percent <= 0) return Target(0, 0f)
        val x = percent.coerceAtMost(100) / 100f * steps
        val lo = floor(x).toInt().coerceIn(0, steps)
        val hi = (lo + 1).coerceAtMost(steps)
        val target = stepDb[lo] + (stepDb[hi] - stepDb[lo]) * (x - lo)
        val index = (lo..hi).first { stepDb[it] >= target - Epsilon }
        return Target(index, (target - stepDb[index]).coerceAtMost(0f))
    }

    fun percentFor(index: Int, trimDb: Float, stepDb: List<Float>): Int {
        val steps = stepDb.size - 1
        if (steps <= 0) return 0
        val target = stepDb[index.coerceIn(0, steps)] + trimDb
        if (target <= stepDb[0]) return 0
        for (i in 0 until steps) {
            if (target <= stepDb[i + 1] + Epsilon) {
                val span = stepDb[i + 1] - stepDb[i]
                val fraction = if (span <= 0f) 1f else ((target - stepDb[i]) / span).coerceIn(0f, 1f)
                return ((i + fraction) / steps * 100).roundToInt().coerceIn(0, 100)
            }
        }
        return 100
    }

    /** Fixes non-finite entries (index 0 is often -inf) and enforces a non-decreasing table. */
    fun normalize(raw: List<Float>): List<Float> {
        if (raw.size < 2) return raw
        val out = raw.toMutableList()
        if (!out[0].isFinite()) out[0] = (out.getOrNull(1)?.takeIf { it.isFinite() } ?: -MuteFloorDb) - MuteGapDb
        for (i in 1 until out.size) {
            if (!out[i].isFinite()) out[i] = out[i - 1]
            if (out[i] < out[i - 1]) out[i] = out[i - 1]
        }
        return out
    }

    /** Used when the platform cannot report per-step dB: amplitude proportional to the step index. */
    fun linearTable(steps: Int): List<Float> =
        List(steps + 1) { i -> if (i == 0) -60f else (20 * log10(i / steps.toFloat())) }

    fun isUsable(table: List<Float>): Boolean = table.size >= 2 && table.last() - table.first() > 1f

    private const val Epsilon = 1e-3f
    private const val MuteGapDb = 12f
    private const val MuteFloorDb = 0f
}
