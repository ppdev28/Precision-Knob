package com.sonicdial.app.ui.volume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicdial.app.AppContainer
import com.sonicdial.app.audio.AudioStreamType
import com.sonicdial.app.audio.StreamVolume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VolumeUiState(
    val stream: AudioStreamType = AudioStreamType.Media,
    val volume: StreamVolume = StreamVolume(level = 0, min = 0, max = 0, muted = false),
)

class VolumeViewModel(private val container: AppContainer) : ViewModel() {
    private val controller = container.volume
    private val _state = MutableStateFlow(VolumeUiState())
    val state: StateFlow<VolumeUiState> = _state

    init {
        viewModelScope.launch {
            val settings = container.settings.settings.first()
            selectStream(if (settings.restoreLastStream) settings.lastStream else settings.defaultStream, persist = false)
        }
        // Keep the dial in sync with hardware keys and other apps.
        viewModelScope.launch { controller.changes().collect { refresh() } }
    }

    fun selectStream(stream: AudioStreamType, persist: Boolean = true) {
        _state.value = VolumeUiState(stream, controller.read(stream))
        if (persist) viewModelScope.launch { container.settings.setLastStream(stream) }
    }

    fun setPercent(percent: Int) = act { controller.setPercent(it.stream, percent) }
    fun stepUp() = act { controller.step(it.stream, +1) }
    fun stepDown() = act { controller.step(it.stream, -1) }
    fun toggleMute() = act { controller.toggleMute(it.stream) }

    private fun refresh() = _state.update { it.copy(volume = controller.read(it.stream)) }

    private inline fun act(action: (VolumeUiState) -> Unit) {
        action(_state.value)
        refresh()
    }
}
