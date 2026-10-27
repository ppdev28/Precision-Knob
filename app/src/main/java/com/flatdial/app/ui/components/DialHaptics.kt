package com.flatdial.app.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Detent "click" for the dial, played straight on the device vibrator (needs the VIBRATE permission).
 * Compose's performHapticFeedback is skipped on purpose: it depends on the system "touch feedback"
 * switch and its TEXT_HANDLE_MOVE effect is silent on many phones.
 */
class DialHaptics(context: Context) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    // EFFECT_TICK is too faint to feel on many motors (only the stronger clicks were noticeable), so every
    // detent uses a real click and the major ones (every 10%, ends) a heavy click.
    private val click: VibrationEffect? = effect(VibrationEffect.EFFECT_CLICK, durationMs = 14, amplitude = 190)
    private val heavy: VibrationEffect? = effect(VibrationEffect.EFFECT_HEAVY_CLICK, durationMs = 24, amplitude = 255)

    /** Every detent clicks; [strong] marks major ones (every 10%, and both ends) with a heavier click. */
    fun detent(strong: Boolean) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val e = (if (strong) heavy else click) ?: return
        runCatching { v.vibrate(e) }
    }

    private fun effect(predefinedId: Int, durationMs: Long, amplitude: Int): VibrationEffect? {
        val v = vibrator ?: return null
        // Prefer the phone's own tick/click waveform when its motor supports it, else a short pulse.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            v.areEffectsSupported(predefinedId).firstOrNull() == Vibrator.VIBRATION_EFFECT_SUPPORT_YES
        ) {
            return VibrationEffect.createPredefined(predefinedId)
        }
        val amp = if (v.hasAmplitudeControl()) amplitude else VibrationEffect.DEFAULT_AMPLITUDE
        return VibrationEffect.createOneShot(durationMs, amp)
    }
}

@Composable
fun rememberDialHaptics(): DialHaptics {
    val context = LocalContext.current
    return remember(context) { DialHaptics(context) }
}
