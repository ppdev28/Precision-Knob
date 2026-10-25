package com.flatdial.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { Dark, Light, System }

data class AppSettings(
    val showPercent: Boolean = true,
    val haptics: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.Dark,
    val eqEnabled: Boolean = false,
    val selectedProfileId: String? = null,
    /** Master switch: when false the audio effects are released and no background service runs. */
    val processingEnabled: Boolean = true,
)

private val Context.dataStore by preferencesDataStore(name = "flatdial_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val showPercent = booleanPreferencesKey("show_percent")
        val haptics = booleanPreferencesKey("haptics")
        val themeMode = stringPreferencesKey("theme_mode")
        val eqEnabled = booleanPreferencesKey("eq_enabled")
        val selectedProfile = stringPreferencesKey("selected_profile")
        val processing = booleanPreferencesKey("processing_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            showPercent = prefs[Keys.showPercent] ?: defaults.showPercent,
            haptics = prefs[Keys.haptics] ?: defaults.haptics,
            themeMode = prefs.enumOrNull<ThemeMode>(Keys.themeMode) ?: defaults.themeMode,
            eqEnabled = prefs[Keys.eqEnabled] ?: defaults.eqEnabled,
            selectedProfileId = prefs[Keys.selectedProfile],
            processingEnabled = prefs[Keys.processing] ?: defaults.processingEnabled,
        )
    }

    suspend fun setShowPercent(value: Boolean) = edit { it[Keys.showPercent] = value }
    suspend fun setHaptics(value: Boolean) = edit { it[Keys.haptics] = value }
    suspend fun setThemeMode(value: ThemeMode) = edit { it[Keys.themeMode] = value.name }

    suspend fun setProcessingEnabled(value: Boolean) = edit { it[Keys.processing] = value }
    suspend fun setEqEnabled(value: Boolean) = edit { it[Keys.eqEnabled] = value }
    suspend fun setSelectedProfile(id: String?) = edit { if (id == null) it.remove(Keys.selectedProfile) else it[Keys.selectedProfile] = id }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { block(it) }
    }
}

private inline fun <reified E : Enum<E>> Preferences.enumOrNull(key: Preferences.Key<String>): E? =
    this[key]?.let { name -> enumValues<E>().firstOrNull { it.name == name } }
