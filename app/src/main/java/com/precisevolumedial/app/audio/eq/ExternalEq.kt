package com.precisevolumedial.app.audio.eq

import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

/** Manual inverse of the tone settings of the radio or speaker connected to the phone. */
object ExternalEq {
    /** Legacy layout, retained for existing installations and as the default. */
    val BandsHz = listOf(60f, 230f, 910f, 3600f, 12000f)
    const val MinDb = -12
    const val MaxDb = 12
    const val Bands = 5
    const val MinBands = 1
    const val MaxBands = 10
    const val MinHz = 20
    const val MaxHz = 20_000
    private const val Q = 0.7f

    val Flat = List(Bands) { 0 }

    /** Reference curves: presets are interpolated at the user's actual frequencies. */
    val Presets: List<Pair<String, List<Int>>> = listOf(
        "flat" to Flat,
        "bass" to listOf(6, 3, 0, 0, 0),
        "treble" to listOf(0, 0, 0, 3, 6),
        "vshape" to listOf(5, 2, -2, 2, 5),
    )

    fun defaultFrequencies(count: Int): List<Float> {
        require(count in MinBands..MaxBands)
        return when (count) {
            1 -> listOf(1000f)
            Bands -> BandsHz
            10 -> listOf(31f, 63f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
            else -> logSpaced(count, BandsHz.first(), BandsHz.last()).map { it.roundToInt().toFloat() }
        }
    }

    /** Preserve the approximate tone curve when changing the band layout. */
    fun resampleGains(frequencies: List<Float>, gainsDb: List<Int>, target: List<Float>): List<Int> {
        val points = frequencies.zip(gainsDb).sortedBy { it.first }
        return target.map { hz ->
            when {
                points.isEmpty() -> 0
                hz <= points.first().first -> points.first().second
                hz >= points.last().first -> points.last().second
                else -> {
                    val upper = points.indexOfFirst { it.first >= hz }
                    val (lowHz, lowGain) = points[upper - 1]
                    val (highHz, highGain) = points[upper]
                    val fraction = ln(hz / lowHz) / ln(highHz / lowHz)
                    (lowGain + fraction * (highGain - lowGain)).roundToInt()
                }
            }.coerceIn(MinDb, MaxDb)
        }
    }

    fun presetGains(key: String, frequencies: List<Float>): List<Int> =
        resampleGains(BandsHz, Presets.firstOrNull { it.first == key }?.second ?: Flat, frequencies)

    /** Filters that cancel the external device's gains at the configured centre frequencies. */
    fun inverseFilters(gainsDb: List<Int>, frequencies: List<Float> = BandsHz): List<ParametricFilter> =
        frequencies.zip(gainsDb).filter { it.second != 0 }.map { (hz, g) ->
            ParametricFilter(FilterType.Peak, hz, -g.toFloat(), Q)
        }

    /** The inverse boosts wherever the device cuts, so reserve that much headroom. */
    fun preampDb(gainsDb: List<Int>): Float = -max(0, gainsDb.maxOfOrNull { -it } ?: 0).toFloat()

    fun parse(text: String?, count: Int = Bands): List<Int> {
        val values = text?.split(',')?.map { it.trim().toIntOrNull() }
        return if (values != null && values.size == count && values.all { it != null }) {
            values.map { it!!.coerceIn(MinDb, MaxDb) }
        } else List(count) { 0 }
    }

    /** Missing frequencies migrate the original five-band settings without losing their gains. */
    fun parseFrequencies(text: String?): List<Float> {
        val values = text?.split(',')?.map { it.trim().toFloatOrNull() }
        return if (values != null && values.size in MinBands..MaxBands &&
            values.all { it != null && it.isFinite() && it in MinHz.toFloat()..MaxHz.toFloat() }
        ) values.map { it!! } else BandsHz
    }

    fun serialize(gainsDb: List<Int>): String = gainsDb.joinToString(",")
    fun serializeFrequencies(frequencies: List<Float>): String = frequencies.joinToString(",")
}
