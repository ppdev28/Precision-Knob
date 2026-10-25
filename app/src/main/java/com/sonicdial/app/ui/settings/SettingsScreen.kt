package com.sonicdial.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonicdial.app.BuildConfig
import com.sonicdial.app.R
import com.sonicdial.app.audio.AudioStreamType
import com.sonicdial.app.data.ThemeMode
import com.sonicdial.app.ui.AppViewModelProvider
import com.sonicdial.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showStreamDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            SectionHeader(R.string.settings_section_audio)
            ClickRow(
                title = stringResource(R.string.settings_default_stream),
                summary = stringResource(settings.defaultStream.label),
                onClick = { showStreamDialog = true },
            )
            SwitchRow(stringResource(R.string.settings_show_percent), null, settings.showPercent, viewModel::setShowPercent)
            SwitchRow(stringResource(R.string.settings_haptics), stringResource(R.string.settings_haptics_summary), settings.haptics, viewModel::setHaptics)
            SwitchRow(stringResource(R.string.settings_restore_last), null, settings.restoreLastStream, viewModel::setRestoreLastStream)

            HorizontalDivider(Modifier.padding(vertical = Spacing.s))
            SectionHeader(R.string.settings_section_appearance)
            ClickRow(
                title = stringResource(R.string.settings_theme),
                summary = stringResource(settings.themeMode.label()),
                onClick = { showThemeDialog = true },
            )

            HorizontalDivider(Modifier.padding(vertical = Spacing.s))
            SectionHeader(R.string.settings_section_about)
            InfoRow(stringResource(R.string.settings_version), BuildConfig.VERSION_NAME)
            InfoRow(stringResource(R.string.settings_privacy), stringResource(R.string.settings_privacy_summary))
            InfoRow(stringResource(R.string.settings_permissions), stringResource(R.string.settings_permissions_summary))
        }
    }

    if (showStreamDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_default_stream),
            options = AudioStreamType.entries,
            selected = settings.defaultStream,
            labelOf = { stringResource(it.label) },
            onSelect = { viewModel.setDefaultStream(it); showStreamDialog = false },
            onDismiss = { showStreamDialog = false },
        )
    }
    if (showThemeDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_theme),
            options = ThemeMode.entries,
            selected = settings.themeMode,
            labelOf = { stringResource(it.label()) },
            onSelect = { viewModel.setThemeMode(it); showThemeDialog = false },
            onDismiss = { showThemeDialog = false },
        )
    }
}

private fun ThemeMode.label(): Int = when (this) {
    ThemeMode.Dark -> R.string.theme_dark
    ThemeMode.Light -> R.string.theme_light
    ThemeMode.System -> R.string.theme_system
}

@Composable
private fun SectionHeader(title: Int) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
    )
}

@Composable
private fun ClickRow(title: String, summary: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun SwitchRow(title: String, summary: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.clickable(role = Role.Switch) { onChange(!checked) },
    )
}

@Composable
private fun InfoRow(title: String, summary: String) {
    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(summary) })
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    labelOf: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = option == selected, role = Role.RadioButton) { onSelect(option) }
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = null)
                        Text(labelOf(option), modifier = Modifier.padding(start = Spacing.m))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}
