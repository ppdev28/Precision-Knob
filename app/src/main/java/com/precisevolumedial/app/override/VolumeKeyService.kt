package com.precisevolumedial.app.override

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.precisevolumedial.app.PreciseVolumeDialApplication
import com.precisevolumedial.app.audio.AudioStreamType
import com.precisevolumedial.app.data.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Volume button override. An accessibility service is the only non-root way to see the volume keys
 * in every app, on the lock screen and with the screen off; returning true from [onKeyEvent]
 * consumes the key so the system popup never appears, and the media volume moves by the user's step.
 */
class VolumeKeyService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile private var settings = AppSettings()
    private var popup: VolumePopup? = null

    override fun onServiceConnected() {
        val container = (application as PreciseVolumeDialApplication).container
        popup = VolumePopup(this, container.volume)
        scope.launch {
            container.settings.settings.collect {
                settings = it
                popup?.update(it.popupStyle, it.popupSide)
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val up = when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> true
            KeyEvent.KEYCODE_VOLUME_DOWN -> false
            else -> return false
        }
        val audio = getSystemService(AudioManager::class.java)
        if (!shouldOverride(settings, deviceState(), inCall = audio.mode != AudioManager.MODE_NORMAL)) return false

        // Act on every ACTION_DOWN (holding the key repeats it); swallow ACTION_UP so the system sees nothing.
        if (event.action == KeyEvent.ACTION_DOWN) {
            val controller = (application as PreciseVolumeDialApplication).container.volume
            val stream = AudioStreamType.Media
            // Native steps have no sub-step resolution: one key press is one system step.
            val step = if (controller.isFine(stream)) settings.overrideStep else 1
            val volume = controller.step(stream, if (up) step else -step)
            if (getSystemService(PowerManager::class.java).isInteractive) popup?.show(stream)
        }
        return true
    }

    private fun deviceState(): DeviceState = when {
        !getSystemService(PowerManager::class.java).isInteractive -> DeviceState.ScreenOff
        getSystemService(KeyguardManager::class.java).isKeyguardLocked -> DeviceState.Locked
        else -> DeviceState.Unlocked
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        popup?.hide(immediately = true)
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        /** True when the user has switched the service on in the system accessibility settings. */
        fun isEnabled(context: Context): Boolean {
            val self = ComponentName(context, VolumeKeyService::class.java)
            val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return enabled?.split(':')?.any { ComponentName.unflattenFromString(it) == self } == true
        }
    }
}
