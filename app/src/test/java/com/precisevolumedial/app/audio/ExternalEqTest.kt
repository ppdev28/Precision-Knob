package com.precisevolumedial.app.audio

import com.precisevolumedial.app.audio.eq.EqProfile
import com.precisevolumedial.app.audio.eq.EqState
import com.precisevolumedial.app.audio.eq.ExternalEq
import com.precisevolumedial.app.audio.eq.FilterType
import com.precisevolumedial.app.audio.eq.ParametricFilter
import com.precisevolumedial.app.audio.eq.responseDb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalEqTest {
    @Test fun flatHasNoFilters() {
        assertTrue(ExternalEq.inverseFilters(ExternalEq.Flat).isEmpty())
        assertEquals(0f, ExternalEq.preampDb(ExternalEq.Flat), 0f)
    }

    @Test fun inverseCancelsTheBassBoostOfTheDevice() {
        val device = listOf(6, 0, 0, 0, 0)
        val inverse = ExternalEq.inverseFilters(device)
        assertEquals(-6f, inverse.single().gainDb, 0f)
        assertTrue(inverse.responseDb(60f) < -5f)
        assertEquals(0f, inverse.responseDb(10_000f), 0.5f)
        // The device only boosts, so the inverse only cuts: no extra headroom needed.
        assertEquals(0f, ExternalEq.preampDb(device), 0f)
    }

    @Test fun deviceCutsAreCompensatedWithPreamp() {
        assertEquals(-4f, ExternalEq.preampDb(listOf(0, -4, 0, 2, 0)), 0f)
    }

    @Test fun parseRoundTripAndFallback() {
        val g = listOf(6, 3, 0, -2, 12)
        assertEquals(g, ExternalEq.parse(ExternalEq.serialize(g)))
        assertEquals(ExternalEq.Flat, ExternalEq.parse(null))
        assertEquals(ExternalEq.Flat, ExternalEq.parse("1,2"))
        assertEquals(listOf(12, -12, 0, 0, 0), ExternalEq.parse("30,-30,0,0,0"))
    }

    @Test fun customFrequenciesReachTheAudioFilters() {
        val s = EqState(
            externalEnabled = true,
            externalGains = listOf(6, -4, 0),
            externalFrequencies = listOf(80f, 1500f, 8000f),
        )
        assertEquals(listOf(80f, 1500f), s.fxFilters.map { it.freqHz })
        assertEquals(listOf(-6f, 4f), s.fxFilters.map { it.gainDb })
        assertEquals(-4f, s.fxPreampDb, 0f)
        assertEquals(-6f, s.fxFilters.first().magnitudeDb(80f), 0.01f)
    }

    @Test fun variableBandSettingsRoundTripIncludingLimits() {
        val frequencies = listOf(20f, 200f, 20_000f)
        assertEquals(frequencies, ExternalEq.parseFrequencies(ExternalEq.serializeFrequencies(frequencies)))
        assertEquals(listOf(-12, 0, 12), ExternalEq.parse("-30,0,30", frequencies.size))
        assertEquals(listOf(7), ExternalEq.parse("7", 1))
        assertEquals(List(10) { 2 }, ExternalEq.parse(ExternalEq.serialize(List(10) { 2 }), 10))
    }

    @Test fun legacySettingsKeepTheirLayoutAndGains() {
        val frequencies = ExternalEq.parseFrequencies(null)
        assertEquals(ExternalEq.BandsHz, frequencies)
        assertEquals(listOf(6, 3, 0, -2, 12), ExternalEq.parse("6,3,0,-2,12", frequencies.size))
    }

    @Test fun invalidSavedBandsFallBackWithoutDroppingMalformedValues() {
        listOf("", "NaN", "Infinity", "19", "20001", "80,nope,1000", List(11) { "100" }.joinToString(",")).forEach {
            assertEquals(ExternalEq.BandsHz, ExternalEq.parseFrequencies(it))
        }
        assertEquals(List(3) { 0 }, ExternalEq.parse("6,nope,3,0", 3))
        assertEquals(List(3) { 0 }, ExternalEq.parse("6,3", 3))
    }

    @Test fun everyBandCountHasAValidDefaultLayout() {
        (ExternalEq.MinBands..ExternalEq.MaxBands).forEach { count ->
            val frequencies = ExternalEq.defaultFrequencies(count)
            assertEquals(count, frequencies.size)
            assertTrue(frequencies.all { it in 20f..20_000f })
            assertTrue(frequencies.zipWithNext().all { (a, b) -> a < b })
        }
        assertEquals(ExternalEq.BandsHz, ExternalEq.defaultFrequencies(5))
    }

    @Test fun resizingInterpolatesGainsOnALogarithmicFrequencyScale() {
        assertEquals(
            listOf(6, 6, 3, 0, 0),
            ExternalEq.resampleGains(listOf(100f, 10_000f), listOf(6, 0), listOf(20f, 100f, 1000f, 10_000f, 20_000f)),
        )
        // A band can move past another one; interpolation still follows frequencies, not row order.
        assertEquals(listOf(3), ExternalEq.resampleGains(listOf(10_000f, 100f), listOf(0, 6), listOf(1000f)))
    }

    @Test fun presetsFollowTheChosenFrequencies() {
        ExternalEq.Presets.forEach { (key, reference) ->
            assertEquals(reference, ExternalEq.presetGains(key, ExternalEq.BandsHz))
            assertEquals(List(10) { 0 }, ExternalEq.presetGains("flat", ExternalEq.defaultFrequencies(10)))
        }
        assertEquals(listOf(0, 6), ExternalEq.presetGains("bass", listOf(12_000f, 60f)))
    }

    private val headphones = EqProfile("h", "H", null, -3f, listOf(ParametricFilter(FilterType.Peak, 1000f, 4f, 1f)), "")

    @Test fun antiEqWorksWithoutHeadphoneCorrection() {
        val s = EqState(enabled = false, profiles = listOf(headphones), selectedId = "h", externalEnabled = true, externalGains = listOf(6, 0, 0, 0, 0))
        assertTrue(s.fxEnabled)
        assertEquals(1, s.fxFilters.size) // only the anti-EQ band
        assertEquals(-6f, s.fxFilters.single().gainDb, 0f)
        assertEquals(0f, s.fxPreampDb, 0f) // the headphone preamp is not applied while its switch is off
    }

    @Test fun headphoneCorrectionIgnoresAntiEq() {
        val s = EqState(enabled = true, profiles = listOf(headphones), selectedId = "h", externalEnabled = false, externalGains = listOf(6, 0, 0, 0, 0))
        assertEquals(headphones.filters, s.fxFilters)
        assertEquals(-3f, s.fxPreampDb, 0f)
        // The screen curve is always the headphone profile alone.
        assertEquals(headphones.filters, s.copy(externalEnabled = true).filters)
    }

    @Test fun bothOnAddUp() {
        val s = EqState(enabled = true, profiles = listOf(headphones), selectedId = "h", externalEnabled = true, externalGains = listOf(0, -4, 0, 0, 0))
        assertEquals(2, s.fxFilters.size)
        assertEquals(-7f, s.fxPreampDb, 0f)
    }
}
