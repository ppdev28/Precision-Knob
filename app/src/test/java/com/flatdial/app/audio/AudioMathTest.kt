package com.flatdial.app.audio

import com.flatdial.app.audio.eq.FilterType
import com.flatdial.app.audio.eq.ParametricEqParser
import com.flatdial.app.audio.eq.ParametricFilter
import com.flatdial.app.data.AutoEqEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioMathTest {
    private val sampleEq = """
        Preamp: -6.3 dB
        Filter 1: ON LSC Fc 105 Hz Gain 6.5 dB Q 0.70
        Filter 2: ON PK Fc 125 Hz Gain -2.7 dB Q 0.55
        Filter 3: OFF PK Fc 8445 Hz Gain 3.3 dB Q 1.61
        Filter 4: ON HSC Fc 10000 Hz Gain -3.1 dB Q 0.70
        Filter 5: ON LP Fc 18000 Hz
    """.trimIndent()

    @Test fun parsesParametricEq() {
        val eq = ParametricEqParser.parse(sampleEq)
        assertEquals(-6.3f, eq.preampDb, 0f)
        assertEquals(3, eq.filters.size)
        assertEquals(FilterType.LowShelf, eq.filters[0].type)
        assertEquals(1, eq.skipped)
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsGarbage() {
        ParametricEqParser.parse("hello")
    }

    @Test fun peakFilterHasItsGainAtCenter() {
        val f = ParametricFilter(FilterType.Peak, 1000f, 6f, 1f)
        assertEquals(6f, f.magnitudeDb(1000f), 0.05f)
        assertEquals(0f, f.magnitudeDb(20f), 0.1f)
    }

    @Test fun shelvesReachTheirGain() {
        assertEquals(6f, ParametricFilter(FilterType.LowShelf, 200f, 6f, 0.7f).magnitudeDb(20f), 0.2f)
        assertEquals(-4f, ParametricFilter(FilterType.HighShelf, 5000f, -4f, 0.7f).magnitudeDb(20000f), 0.4f)
    }

    // Typical 15-step table, ~3 dB per step.
    private val table = List(16) { -45f + it * 3f }

    @Test fun wholeStepsNeedNoTrim() {
        val t = VolumeMapper.targetFor(100, table)
        assertEquals(15, t.index); assertEquals(0f, t.trimDb, 0f)
    }

    @Test fun inBetweenPercentUsesNextStepWithTrim() {
        val t = VolumeMapper.targetFor(50, table) // x = 7.5 -> 8th step minus half a step
        assertEquals(8, t.index)
        assertEquals(-1.5f, t.trimDb, 0.01f)
    }

    @Test fun everyPercentRoundTripsAndIsMonotonic() {
        var last = -1
        for (p in 0..100) {
            val t = VolumeMapper.targetFor(p, table)
            val back = VolumeMapper.percentFor(t.index, t.trimDb, table)
            assertEquals("percent $p", p, back)
            assertTrue(back >= last); last = back
        }
    }

    @Test fun normalizeFixesInfiniteMuteStep() {
        val n = VolumeMapper.normalize(listOf(Float.NEGATIVE_INFINITY, -30f, -27f))
        assertTrue(n[0].isFinite() && n[0] < n[1])
    }

    @Test fun parsesAutoEqIndexLines() {
        val entries = AutoEqEntry.parseIndex(
            """
            # Index
            - [1Custom SA02](./crinacle/711%20in-ear/1Custom%20SA02) by crinacle on 711
            - [1MORE Crystal Piston](./Innerfidelity/in-ear/1MORE%20Crystal%20Piston) by Innerfidelity
            - [1MORE Aero (ANC Off)](./HypetheSonics/GRAS%20RA0045%20in-ear/1MORE%20Aero%20(ANC%20Off)) by HypetheSonics on GRAS RA0045
            """.trimIndent(),
        )
        assertEquals(3, entries.size)
        assertEquals("Innerfidelity", entries[1].source)
        assertEquals("", entries[1].rig)
        assertEquals("1MORE Aero (ANC Off)", entries[2].name)
        assertEquals("HypetheSonics", entries[2].source)
        assertEquals("GRAS RA0045", entries[2].rig)
        assertEquals(
            "https://raw.githubusercontent.com/jaakkopasanen/AutoEq/master/results/crinacle/711%20in-ear/1Custom%20SA02/1Custom%20SA02%20ParametricEQ.txt",
            entries[0].parametricUrl,
        )
    }
}

class EqFitTest {
    private val bands = listOf(0f to 120f, 120f to 480f, 480f to 1900f, 1900f to 7500f, 7500f to 20000f)

    @Test fun flatChainGivesFlatBands() {
        val g = com.flatdial.app.audio.eq.EqFit.bandGains(emptyList(), bands, -15f, 15f)
        g.forEach { assertEquals(0f, it, 0.001f) }
    }

    @Test fun boostLandsInTheRightBandAndIsClamped() {
        val f = listOf(ParametricFilter(FilterType.Peak, 1000f, 20f, 1f))
        val g = com.flatdial.app.audio.eq.EqFit.bandGains(f, bands, -15f, 15f)
        assertTrue(g[2] > g[0] && g[2] > g[4])
        assertTrue(g.all { it <= 15f })
    }
}
