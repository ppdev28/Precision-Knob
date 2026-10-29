package com.precisevolumedial.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.precisevolumedial.app.audio.eq.ExternalEq
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { Dark, Light, System }

/** Look of the on-screen volume popup shown by the volume button override. */
enum class PopupStyle { Accent, Dark, Light }

enum class PopupSide { Right, Left }

data class AppSettings(
    val showPercent: Boolean = true,
    val haptics: Boolean = true,
    /** Dial detent size in percent. */
    val dialStep: Int = 5,
    val themeMode: ThemeMode = ThemeMode.Dark,
    val eqEnabled: Boolean = false,
    val selectedProfileId: String? = null,
    /** Master switch: when false the audio effects are released and no background service runs. */
    val processingEnabled: Boolean = true,
    /** Volume button override: the hardware keys move the media volume in fine steps in any app. */
    val overrideEnabled: Boolean = false,
    val overrideUnlocked: Boolean = true,
    val overrideLocked: Boolean = true,
    val overrideScreenOff: Boolean = true,
    /** Percent moved by each key press. */
    val overrideStep: Int = 1,
    val popupStyle: PopupStyle = PopupStyle.Accent,
    val popupSide: PopupSide = PopupSide.Right,
    /** Anti-EQ: cancel the tone settings of the radio/speaker the phone is connected to. */
    val externalEqEnabled: Boolean = false,
    /** Gains (dB) of the external device on the five anti-EQ bands. */
    val externalEqGains: List<Int> = ExternalEq.Flat,
)

/** Allowed dial detent sizes (all divide 100). */
val DialSteps = listOf(1, 2, 5, 10)

/** Allowed volume key step sizes in percent. */
val OverrideSteps = listOf(1, 2, 5)

private val Context.dataStore by preferencesDataStore(name = "precisevolumedial_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val showPercent = booleanPreferencesKey("show_percent")
        val haptics = booleanPreferencesKey("haptics")
        val dialStep = intPreferencesKey("dial_step")
        val themeMode = stringPreferencesKey("theme_mode")
        val eqEnabled = booleanPreferencesKey("eq_enabled")
        val selectedProfile = stringPreferencesKey("selected_profile")
        val processing = booleanPreferencesKey("processing_enabled")
        val overrideEnabled = booleanPreferencesKey("override_enabled")
        val overrideUnlocked = booleanPreferencesKey("override_unlocked")
        val overrideLocked = booleanPreferencesKey("override_locked")
        val overrideScreenOff = booleanPreferencesKey("override_screen_off")
        val overrideStep = intPreferencesKey("override_step")
        val popupStyle = stringPreferencesKey("popup_style")
        val popupSide = stringPreferencesKey("popup_side")
        val externalEqEnabled = booleanPreferencesKey("external_eq_enabled")
        val externalEqGains = stringPreferencesKey("external_eq_gains")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            showPercent = prefs[Keys.showPercent] ?: defaults.showPercent,
            haptics = prefs[Keys.haptics] ?: defaults.haptics,
            dialStep = prefs[Keys.dialStep]?.takeIf { it in DialSteps } ?: defaults.dialStep,
            themeMode = prefs.enumOrNull<ThemeMode>(Keys.themeMode) ?: defaults.themeMode,
            eqEnabled = prefs[Keys.eqEnabled] ?: defaults.eqEnabled,
            selectedProfileId = prefs[Keys.selectedProfile],
            processingEnabled = prefs[Keys.processing] ?: defaults.processingEnabled,
            overrideEnabled = prefs[Keys.overrideEnabled] ?: defaults.overrideEnabled,
            overrideUnlocked = prefs[Keys.overrideUnlocked] ?: defaults.overrideUnlocked,
            overrideLocked = prefs[Keys.overrideLocked] ?: defaults.overrideLocked,
            overrideScreenOff = prefs[Keys.overrideScreenOff] ?: defaults.overrideScreenOff,
            overrideStep = prefs[Keys.overrideStep]?.takeIf { it in OverrideSteps } ?: defaults.overrideStep,
            popupStyle = prefs.enumOrNull<PopupStyle>(Keys.popupStyle) ?: defaults.popupStyle,
            popupSide = prefs.enumOrNull<PopupSide>(Keys.popupSide) ?: defaults.popupSide,
            externalEqEnabled = prefs[Keys.externalEqEnabled] ?: defaults.externalEqEnabled,
            externalEqGains = ExternalEq.parse(prefs[Keys.externalEqGains]),
        )
    }

    suspend fun setShowPercent(value: Boolean) = edit { it[Keys.showPercent] = value }
    suspend fun setHaptics(value: Boolean) = edit { it[Keys.haptics] = value }
    suspend fun setDialStep(value: Int) = edit { it[Keys.dialStep] = value }
    suspend fun setThemeMode(value: ThemeMode) = edit { it[Keys.themeMode] = value.name }

    suspend fun setProcessingEnabled(value: Boolean) = edit { it[Keys.processing] = value }
    suspend fun setOverrideEnabled(value: Boolean) = edit { it[Keys.overrideEnabled] = value }
    suspend fun setOverrideUnlocked(value: Boolean) = edit { it[Keys.overrideUnlocked] = value }
    suspend fun setOverrideLocked(value: Boolean) = edit { it[Keys.overrideLocked] = value }
    suspend fun setOverrideScreenOff(value: Boolean) = edit { it[Keys.overrideScreenOff] = value }
    suspend fun setOverrideStep(value: Int) = edit { it[Keys.overrideStep] = value }
    suspend fun setPopupStyle(value: PopupStyle) = edit { it[Keys.popupStyle] = value.name }
    suspend fun setPopupSide(value: PopupSide) = edit { it[Keys.popupSide] = value.name }
    suspend fun setExternalEqEnabled(value: Boolean) = edit { it[Keys.externalEqEnabled] = value }
    suspend fun setExternalEqGains(value: List<Int>) = edit { it[Keys.externalEqGains] = ExternalEq.serialize(value) }
    suspend fun setEqEnabled(value: Boolean) = edit { it[Keys.eqEnabled] = value }
    suspend fun setSelectedProfile(id: String?) = edit { if (id == null) it.remove(Keys.selectedProfile) else it[Keys.selectedProfile] = id }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { block(it) }
    }
}

private inline fun <reified E : Enum<E>> Preferences.enumOrNull(key: Preferences.Key<String>): E? =
    this[key]?.let { name -> enumValues<E>().firstOrNull { it.name == name } }
