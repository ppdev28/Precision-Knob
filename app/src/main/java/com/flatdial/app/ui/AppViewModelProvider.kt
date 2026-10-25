package com.flatdial.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.flatdial.app.FlatDialApplication
import com.flatdial.app.ui.equalizer.EqualizerViewModel
import com.flatdial.app.ui.equalizer.ProfileSearchViewModel
import com.flatdial.app.ui.settings.SettingsViewModel
import com.flatdial.app.ui.volume.VolumeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { VolumeViewModel(app().container) }
        initializer { EqualizerViewModel(app().container) }
        initializer { ProfileSearchViewModel(app().container) }
        initializer { SettingsViewModel(app().container) }
    }
}

private fun CreationExtras.app(): FlatDialApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FlatDialApplication
