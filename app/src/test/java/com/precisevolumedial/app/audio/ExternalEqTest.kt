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
