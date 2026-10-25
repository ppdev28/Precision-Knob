package com.sonicdial.app.audio.eq

const val EqMinDb = -12f
const val EqMaxDb = 12f

data class EqBand(val label: String, val centerHz: Float)

/** Five manual trim bands layered on top of the headphone profile. */
object ManualBands {
    val bands = listOf(
        EqBand("60 Hz", 60f),
        EqBand("230 Hz", 230f),
        EqBand("910 Hz", 910f),
        EqBand("3.6 kHz", 3600f),
        EqBand("14 kHz", 14000f),
    )
    val flat: List<Float> = List(bands.size) { 0f }

    fun filters(gains: List<Float>): List<ParametricFilter> =
        bands.zip(gains).filter { it.second != 0f }
            .map { (band, gain) -> ParametricFilter(FilterType.Peak, band.centerHz, gain, q = 1f) }
}
