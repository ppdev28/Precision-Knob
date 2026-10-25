package com.sonicdial.app.ui.equalizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonicdial.app.R
import com.sonicdial.app.ui.AppViewModelProvider
import com.sonicdial.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSearchScreen(
    onBack: () -> Unit,
    viewModel: ProfileSearchViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                enabled = !state.loading && !state.downloading,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.m, vertical = Spacing.s),
            )
            if (state.downloading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.search_downloading), modifier = Modifier.padding(Spacing.m))
            }
            state.error?.let { message ->
                Column(modifier = Modifier.padding(Spacing.m)) {
                    Text(stringResource(R.string.search_error, message), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { viewModel.load(forceRefresh = true) }) { Text(stringResource(R.string.search_retry)) }
                }
            }
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.search_loading), modifier = Modifier.padding(top = Spacing.xl * 2))
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (state.query.isNotBlank() && state.results.isEmpty()) {
                        item { Text(stringResource(R.string.search_no_results), modifier = Modifier.padding(Spacing.m)) }
                    }
                    items(state.results, key = { it.path }) { entry ->
                        ListItem(
                            headlineContent = { Text(entry.name) },
                            supportingContent = { Text(listOf(entry.source, entry.rig).filter { it.isNotBlank() }.joinToString(" · ")) },
                            modifier = Modifier.clickable(enabled = !state.downloading) { viewModel.select(entry) },
                        )
                    }
                    if (state.totalMatches > state.results.size) {
                        item {
                            Text(
                                stringResource(R.string.search_refine, state.results.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(Spacing.m),
                            )
                        }
                    }
                }
            }
        }
    }
}
