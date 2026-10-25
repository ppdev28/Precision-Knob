package com.flatdial.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatdial.app.AppContainer
import com.flatdial.app.data.AppSettings
import com.flatdial.app.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.settings

    val settings: StateFlow<AppSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setShowPercent(v: Boolean) = viewModelScope.launch { repo.setShowPercent(v) }
    fun setHaptics(v: Boolean) = viewModelScope.launch { repo.setHaptics(v) }
    fun setProcessingEnabled(v: Boolean) = viewModelScope.launch { repo.setProcessingEnabled(v) }
    fun setThemeMode(v: ThemeMode) = viewModelScope.launch { repo.setThemeMode(v) }
}
