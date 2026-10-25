package com.sonicdial.app.audio.eq

/** Headphone correction profile (typically from AutoEq) that flattens a headphone's response. */
data class EqProfile(
    val id: String,
    val name: String,
    val source: String?,
    val preampDb: Float,
    val filters: List<ParametricFilter>,
    /** Original ParametricEQ.txt text, kept so profiles survive parser changes. */
    val raw: String,
)

data class ParsedEq(val preampDb: Float, val filters: List<ParametricFilter>, val skipped: Int)

/**
 * Parses the AutoEq / EqualizerAPO "ParametricEQ.txt" format:
 * ```
 * Preamp: -6.3 dB
 * Filter 1: ON PK Fc 125 Hz Gain -2.7 dB Q 0.55
 * ```
 * Supports PK, LS/LSC and HS/HSC. Other filter types are skipped and counted.
 */
object ParametricEqParser {
    private val preampRegex = Regex("""^\s*Preamp:\s*(-?[\d.]+)\s*dB""", RegexOption.IGNORE_CASE)
    private val filterRegex = Regex(
        """^\s*Filter\s*\d*:\s*(ON|OFF)\s+(\w+)\s+Fc\s+([\d.]+)\s*Hz(?:\s+Gain\s+(-?[\d.]+)\s*dB)?(?:\s+Q\s+([\d.]+))?""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(text: String): ParsedEq {
        var preamp = 0f
        var skipped = 0
        val filters = mutableListOf<ParametricFilter>()
        for (line in text.lineSequence()) {
            preampRegex.find(line)?.let { preamp = it.groupValues[1].toFloat(); return@let }
            val m = filterRegex.find(line) ?: continue
            if (!m.groupValues[1].equals("ON", ignoreCase = true)) continue
            val type = when (m.groupValues[2].uppercase()) {
                "PK" -> FilterType.Peak
                "LS", "LSC" -> FilterType.LowShelf
                "HS", "HSC" -> FilterType.HighShelf
                else -> { skipped++; continue }
            }
            filters += ParametricFilter(
                type = type,
                freqHz = m.groupValues[3].toFloat(),
                gainDb = m.groupValues[4].ifEmpty { "0" }.toFloat(),
                q = m.groupValues[5].ifEmpty { "0.707" }.toFloat(),
            )
        }
        require(filters.isNotEmpty()) { "No supported filters found" }
        return ParsedEq(preamp, filters, skipped)
    }
}
