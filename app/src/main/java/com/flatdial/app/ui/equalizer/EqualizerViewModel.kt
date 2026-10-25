package com.flatdial.app.ui.equalizer

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatdial.app.AppContainer
import com.flatdial.app.audio.eq.EqState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Thin UI wrapper: the real state lives in [com.flatdial.app.audio.eq.EqController] so it outlives the screen. */
class EqualizerViewModel(container: AppContainer) : ViewModel() {
    private val eq = container.eq
    val state: StateFlow<EqState> = eq.state

    private val _importFailed = MutableStateFlow(false)
    val importFailed: StateFlow<Boolean> = _importFailed

    fun setEnabled(enabled: Boolean) = eq.setEnabled(enabled)
    fun select(id: String?) = eq.select(id)
    fun delete(id: String) = eq.delete(id)
    fun dismissImportError() { _importFailed.value = false }

    /** Imports a ParametricEQ.txt picked with the system file picker. */
    fun importFile(uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            runCatching {
                val (name, text) = withContext(Dispatchers.IO) {
                    val displayName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
                    val body = resolver.openInputStream(uri)!!.use { it.readBytes().take(MaxFileBytes).toByteArray().decodeToString() }
                    (displayName ?: "Imported profile") to body
                }
                eq.importText(name.removeSuffix(".txt").removeSuffix(" ParametricEQ").trim(), "file", text)
            }.onFailure { _importFailed.value = true }
        }
    }

    private companion object {
        const val MaxFileBytes = 256 * 1024
    }
}
