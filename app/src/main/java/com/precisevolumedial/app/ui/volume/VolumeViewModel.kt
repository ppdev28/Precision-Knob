package com.precisevolumedial.app.ui.volume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.precisevolumedial.app.AppContainer
import com.precisevolumedial.app.audio.AudioStreamType
import com.precisevolumedial.app.audio.StreamVolume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VolumeUiState(
    val volume: StreamVolume = StreamVolume(),
)

class VolumeViewModel(private val container: AppContainer) : ViewModel() {
    private val controller = container.volume
    private val stream = AudioStreamType.Media
    private val _state = MutableStateFlow(VolumeUiState(controller.read(stream)))
    val state: StateFlow<VolumeUiState> = _state

    init {
        // Keep the dial in sync with hardware keys and other apps.
        viewModelScope.launch { controller.changes().collect { refresh() } }
        // Fine (1%) control appears/disappears when the audio effect is started or stopped.
        viewModelScope.launch { container.fx.trimAvailable.collect { refresh() } }
    }

    fun setPercent(percent: Int) = act { controller.setPercent(stream, percent) }
    fun stepUp() = act { controller.step(stream, +1) }
    fun stepDown() = act { controller.step(stream, -1) }
    fun toggleMute() = act { controller.toggleMute(stream) }

    fun setProcessingEnabled(enabled: Boolean) {
        viewModelScope.launch { container.settings.setProcessingEnabled(enabled) }
    }

    private fun refresh() = _state.update { it.copy(volume = controller.read(stream)) }

    private inline fun act(action: () -> Unit) {
        action()
        refresh()
    }
}
