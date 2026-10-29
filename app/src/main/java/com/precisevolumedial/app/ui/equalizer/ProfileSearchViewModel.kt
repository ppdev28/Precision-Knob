package com.precisevolumedial.app.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.precisevolumedial.app.AppContainer
import com.precisevolumedial.app.data.AutoEqEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val results: List<AutoEqEntry> = emptyList(),
    val totalMatches: Int = 0,
    val downloading: Boolean = false,
    val done: Boolean = false,
)

class ProfileSearchViewModel(container: AppContainer) : ViewModel() {
    private val eq = container.eq
    private var all: List<AutoEqEntry> = emptyList()

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    init { load(forceRefresh = false) }

    fun load(forceRefresh: Boolean) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { eq.autoEqIndex(forceRefresh) }
                .onSuccess { all = it; _state.update { s -> filtered(s.copy(loading = false)) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: e.javaClass.simpleName) } }
        }
    }

    fun setQuery(query: String) = _state.update { filtered(it.copy(query = query)) }

    fun select(entry: AutoEqEntry) {
        _state.update { it.copy(downloading = true, error = null) }
        viewModelScope.launch {
            runCatching { eq.importFromAutoEq(entry) }
                .onSuccess { _state.update { it.copy(downloading = false, done = true) } }
                .onFailure { e -> _state.update { it.copy(downloading = false, error = e.message ?: e.javaClass.simpleName) } }
        }
    }

    private fun filtered(state: SearchUiState): SearchUiState {
        val terms = state.query.lowercase().split(' ').filter { it.isNotBlank() }
        val matches = if (terms.isEmpty()) emptyList() else all.filter { e ->
            val haystack = "${e.name} ${e.source}".lowercase()
            terms.all { it in haystack }
        }
        return state.copy(results = matches.take(MaxResults), totalMatches = matches.size)
    }

    companion object {
        const val MaxResults = 100
    }
}
