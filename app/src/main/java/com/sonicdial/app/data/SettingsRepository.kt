package com.sonicdial.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sonicdial.app.audio.AudioStreamType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { Dark, Light, System }

data class AppSettings(
    val defaultStream: AudioStreamType = AudioStreamType.Media,
    val showPercent: Boolean = true,
    val haptics: Boolean = true,
    val restoreLastStream: Boolean = false,
    val lastStream: AudioStreamType = AudioStreamType.Media,
    val themeMode: ThemeMode = ThemeMode.Dark,
    val eqEnabled: Boolean = false,
    val selectedProfileId: String? = null,
    val manualGains: List<Float> = List(5) { 0f },
)

private val Context.dataStore by preferencesDataStore(name = "sonicdial_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val defaultStream = stringPreferencesKey("default_stream")
        val showPercent = booleanPreferencesKey("show_percent")
        val haptics = booleanPreferencesKey("haptics")
        val restoreLast = booleanPreferencesKey("restore_last")
        val lastStream = stringPreferencesKey("last_stream")
        val themeMode = stringPreferencesKey("theme_mode")
        val eqEnabled = booleanPreferencesKey("eq_enabled")
        val selectedProfile = stringPreferencesKey("selected_profile")
        val manualGains = stringPreferencesKey("manual_gains")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            defaultStream = prefs.enumOrNull<AudioStreamType>(Keys.defaultStream) ?: defaults.defaultStream,
            showPercent = prefs[Keys.showPercent] ?: defaults.showPercent,
            haptics = prefs[Keys.haptics] ?: defaults.haptics,
            restoreLastStream = prefs[Keys.restoreLast] ?: defaults.restoreLastStream,
            lastStream = prefs.enumOrNull<AudioStreamType>(Keys.lastStream) ?: defaults.lastStream,
            themeMode = prefs.enumOrNull<ThemeMode>(Keys.themeMode) ?: defaults.themeMode,
            eqEnabled = prefs[Keys.eqEnabled] ?: defaults.eqEnabled,
            selectedProfileId = prefs[Keys.selectedProfile],
            manualGains = prefs[Keys.manualGains]?.split(',')?.mapNotNull { it.toFloatOrNull() }
                ?.takeIf { it.size == defaults.manualGains.size } ?: defaults.manualGains,
        )
    }

    suspend fun setDefaultStream(value: AudioStreamType) = edit { it[Keys.defaultStream] = value.name }
    suspend fun setShowPercent(value: Boolean) = edit { it[Keys.showPercent] = value }
    suspend fun setHaptics(value: Boolean) = edit { it[Keys.haptics] = value }
    suspend fun setRestoreLastStream(value: Boolean) = edit { it[Keys.restoreLast] = value }
    suspend fun setLastStream(value: AudioStreamType) = edit { it[Keys.lastStream] = value.name }
    suspend fun setThemeMode(value: ThemeMode) = edit { it[Keys.themeMode] = value.name }

    suspend fun setEqEnabled(value: Boolean) = edit { it[Keys.eqEnabled] = value }
    suspend fun setSelectedProfile(id: String?) = edit { if (id == null) it.remove(Keys.selectedProfile) else it[Keys.selectedProfile] = id }
    suspend fun setManualGains(gains: List<Float>) = edit { it[Keys.manualGains] = gains.joinToString(",") }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { block(it) }
    }
}

private inline fun <reified E : Enum<E>> Preferences.enumOrNull(key: Preferences.Key<String>): E? =
    this[key]?.let { name -> enumValues<E>().firstOrNull { it.name == name } }
