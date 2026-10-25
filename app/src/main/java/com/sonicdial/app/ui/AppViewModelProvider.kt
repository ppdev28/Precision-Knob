package com.sonicdial.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sonicdial.app.SonicDialApplication
import com.sonicdial.app.ui.equalizer.EqualizerViewModel
import com.sonicdial.app.ui.equalizer.ProfileSearchViewModel
import com.sonicdial.app.ui.settings.SettingsViewModel
import com.sonicdial.app.ui.volume.VolumeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { VolumeViewModel(app().container) }
        initializer { EqualizerViewModel(app().container) }
        initializer { ProfileSearchViewModel(app().container) }
        initializer { SettingsViewModel(app().container) }
    }
}

private fun CreationExtras.app(): SonicDialApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SonicDialApplication
