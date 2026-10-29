package com.precisevolumedial.app.audio.eq

import kotlin.math.max

/**
 * "Anti-EQ": the user tells us the tone settings of the radio or speaker the phone is connected to,
 * and we apply the opposite so the sound reaches it flat. The phone cannot read that device's EQ, so the
 * gains are entered by hand on five bands that match the five bands of the system equalizer.
 */
object ExternalEq {
    /** Centre frequencies of the five bands (geometric centres of the usual 5-band hardware equalizer). */
    val BandsHz = listOf(60f, 230f, 910f, 3600f, 12000f)
    const val MinDb = -12
    const val MaxDb = 12
    const val Bands = 5
    private const val Q = 0.7f

    val Flat = List(Bands) { 0 }

    /** One-tap starting points for common radio "sound modes". */
    val Presets: List<Pair<String, List<Int>>> = listOf(
        "flat" to Flat,
        "bass" to listOf(6, 3, 0, 0, 0),
        "treble" to listOf(0, 0, 0, 3, 6),
        "vshape" to listOf(5, 2, -2, 2, 5),
    )

    /** Filters that cancel the external device's [gainsDb] (one per band). */
    fun inverseFilters(gainsDb: List<Int>): List<ParametricFilter> =
        BandsHz.zip(gainsDb).filter { it.second != 0 }.map { (hz, g) ->
            ParametricFilter(FilterType.Peak, hz, -g.toFloat(), Q)
        }

    /** The inverse boosts wherever the device cuts, so lower the overall level by that much to avoid clipping. */
    fun preampDb(gainsDb: List<Int>): Float = -max(0, gainsDb.maxOfOrNull { -it } ?: 0).toFloat()

    fun parse(text: String?): List<Int> {
        val values = text?.split(',')?.mapNotNull { it.trim().toIntOrNull() }
        return if (values != null && values.size == Bands) values.map { it.coerceIn(MinDb, MaxDb) } else Flat
    }

    fun serialize(gainsDb: List<Int>): String = gainsDb.joinToString(",")
}
