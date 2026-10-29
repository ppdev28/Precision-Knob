package com.precisevolumedial.app.override

import com.precisevolumedial.app.data.AppSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverrideRulesTest {
    private val on = AppSettings(overrideEnabled = true)

    @Test fun disabledNeverOverrides() {
        DeviceState.entries.forEach { assertFalse(shouldOverride(AppSettings(), it, inCall = false)) }
    }

    @Test fun eachStateFollowsItsOwnSwitch() {
        assertTrue(shouldOverride(on, DeviceState.Unlocked, false))
        assertFalse(shouldOverride(on.copy(overrideUnlocked = false), DeviceState.Unlocked, false))
        assertFalse(shouldOverride(on.copy(overrideLocked = false), DeviceState.Locked, false))
        assertTrue(shouldOverride(on.copy(overrideLocked = false), DeviceState.ScreenOff, false))
        assertFalse(shouldOverride(on.copy(overrideScreenOff = false), DeviceState.ScreenOff, false))
    }

    @Test fun processingSwitchOffDisablesOverride() {
        DeviceState.entries.forEach { assertFalse(shouldOverride(on.copy(processingEnabled = false), it, inCall = false)) }
    }

    @Test fun callsKeepSystemBehavior() {
        DeviceState.entries.forEach { assertFalse(shouldOverride(on, it, inCall = true)) }
    }
}
