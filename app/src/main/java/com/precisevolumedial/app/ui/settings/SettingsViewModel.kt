package com.precisevolumedial.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.precisevolumedial.app.AppContainer
import com.precisevolumedial.app.data.AppSettings
import com.precisevolumedial.app.data.PopupSide
import com.precisevolumedial.app.data.PopupStyle
import com.precisevolumedial.app.data.ThemeMode
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
    fun setDialStep(v: Int) = viewModelScope.launch { repo.setDialStep(v) }
    fun setProcessingEnabled(v: Boolean) = viewModelScope.launch { repo.setProcessingEnabled(v) }
    fun setThemeMode(v: ThemeMode) = viewModelScope.launch { repo.setThemeMode(v) }
    fun setOverrideEnabled(v: Boolean) = viewModelScope.launch { repo.setOverrideEnabled(v) }
    fun setOverrideUnlocked(v: Boolean) = viewModelScope.launch { repo.setOverrideUnlocked(v) }
    fun setOverrideLocked(v: Boolean) = viewModelScope.launch { repo.setOverrideLocked(v) }
    fun setOverrideScreenOff(v: Boolean) = viewModelScope.launch { repo.setOverrideScreenOff(v) }
    fun setOverrideStep(v: Int) = viewModelScope.launch { repo.setOverrideStep(v) }
    fun setPopupStyle(v: PopupStyle) = viewModelScope.launch { repo.setPopupStyle(v) }
    fun setPopupSide(v: PopupSide) = viewModelScope.launch { repo.setPopupSide(v) }
}
