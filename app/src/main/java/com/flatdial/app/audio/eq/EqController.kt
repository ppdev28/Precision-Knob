package com.flatdial.app.audio.eq

import com.flatdial.app.audio.AudioFxEngine
import com.flatdial.app.data.AutoEqClient
import com.flatdial.app.data.AutoEqEntry
import com.flatdial.app.data.ProfileRepository
import com.flatdial.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EqState(
    val enabled: Boolean = false,
    val profiles: List<EqProfile> = emptyList(),
    val selectedId: String? = null,
    val fxAvailable: Boolean = false,
    val processingOn: Boolean = true,
) {
    val selected: EqProfile? get() = profiles.firstOrNull { it.id == selectedId }
    val filters: List<ParametricFilter> get() = selected?.filters.orEmpty()
    val preampDb: Float get() = selected?.preampDb ?: 0f
}

/**
 * Owns the equalizer state for the whole app (not a ViewModel) so the saved profile is applied
 * as soon as the process starts, even before the Equalizer screen is opened.
 */
class EqController(
    private val settings: SettingsRepository,
    private val profiles: ProfileRepository,
    private val autoEq: AutoEqClient,
    private val fx: AudioFxEngine,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(EqState())
    val state: StateFlow<EqState> = _state

    fun start() {
        scope.launch {
            val saved = settings.settings.first()
            _state.value = EqState(
                enabled = saved.eqEnabled,
                profiles = profiles.profiles.value,
                selectedId = saved.selectedProfileId,
                fxAvailable = fx.eqAvailable.value,
            )
            apply()
        }
        scope.launch { profiles.profiles.collect { list -> _state.update { it.copy(profiles = list) } } }
        scope.launch {
            settings.settings.map { it.processingEnabled }.distinctUntilChanged()
                .collect { on -> _state.update { it.copy(processingOn = on) } }
        }
        scope.launch { fx.eqAvailable.collect { ok -> _state.update { it.copy(fxAvailable = ok) } ; apply() } }
    }

    fun setEnabled(enabled: Boolean) {
        _state.update { it.copy(enabled = enabled) }
        apply()
        scope.launch { settings.setEqEnabled(enabled) }
    }

    fun select(id: String?) {
        _state.update { it.copy(selectedId = id) }
        apply()
        scope.launch { settings.setSelectedProfile(id) }
    }

    fun delete(id: String) {
        profiles.remove(id)
        if (_state.value.selectedId == id) select(null)
    }

    /** Saves and selects a profile from raw ParametricEQ text. Throws IllegalArgumentException if unparseable. */
    suspend fun importText(name: String, source: String?, text: String): EqProfile {
        val profile = kotlinx.coroutines.withContext(Dispatchers.IO) { profiles.add(name, source, text) }
        select(profile.id)
        return profile
    }

    suspend fun importFromAutoEq(entry: AutoEqEntry): EqProfile =
        importText(entry.name, entry.source, autoEq.downloadProfile(entry))

    suspend fun autoEqIndex(forceRefresh: Boolean = false): List<AutoEqEntry> = autoEq.loadIndex(forceRefresh)

    private fun apply() {
        val s = _state.value
        fx.setEq(s.enabled, s.preampDb, s.filters)
    }
}
