package com.precisevolumedial.app.override

import com.precisevolumedial.app.data.AppSettings

/** What the device is doing when a volume key arrives. */
enum class DeviceState { Unlocked, Locked, ScreenOff }

/**
 * Whether the app should take a volume key press. Calls and ringing keep the system behaviour
 * (the keys must control the call or ringer volume there).
 */
fun shouldOverride(settings: AppSettings, state: DeviceState, inCall: Boolean): Boolean {
    // The home-screen master switch (audio processing) turns the whole app off, the override included.
    if (!settings.processingEnabled || !settings.overrideEnabled || inCall) return false
    return when (state) {
        DeviceState.Unlocked -> settings.overrideUnlocked
        DeviceState.Locked -> settings.overrideLocked
        DeviceState.ScreenOff -> settings.overrideScreenOff
    }
}
