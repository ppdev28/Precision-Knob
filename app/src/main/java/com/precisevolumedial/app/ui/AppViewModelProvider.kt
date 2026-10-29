package com.precisevolumedial.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.precisevolumedial.app.PreciseVolumeDialApplication
import com.precisevolumedial.app.ui.equalizer.EqualizerViewModel
import com.precisevolumedial.app.ui.equalizer.ProfileSearchViewModel
import com.precisevolumedial.app.ui.settings.SettingsViewModel
import com.precisevolumedial.app.ui.volume.VolumeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { VolumeViewModel(app().container) }
        initializer { EqualizerViewModel(app().container) }
        initializer { ProfileSearchViewModel(app().container) }
        initializer { SettingsViewModel(app().container) }
    }
}

private fun CreationExtras.app(): PreciseVolumeDialApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PreciseVolumeDialApplication
