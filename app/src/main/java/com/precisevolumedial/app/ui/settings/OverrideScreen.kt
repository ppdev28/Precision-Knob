package com.precisevolumedial.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.precisevolumedial.app.R
import com.precisevolumedial.app.data.OverrideSteps
import com.precisevolumedial.app.data.PopupSide
import com.precisevolumedial.app.data.PopupStyle
import com.precisevolumedial.app.override.VolumeKeyService
import com.precisevolumedial.app.ui.AppViewModelProvider
import com.precisevolumedial.app.ui.theme.Radius
import com.precisevolumedial.app.ui.theme.Spacing
import com.precisevolumedial.app.ui.theme.appSwitchColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverrideScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var serviceOn by remember { mutableStateOf(VolumeKeyService.isEnabled(context)) }
    var showStyle by remember { mutableStateOf(false) }
    var showStep by remember { mutableStateOf(false) }
    var showSide by remember { mutableStateOf(false) }

    // The user flips the service in system settings and comes back: re-read it.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) serviceOn = VolumeKeyService.isEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val openAccessibility = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = Spacing.screenTop),
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
                title = { Text(stringResource(R.string.override_title)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            // Master switch.
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(Radius.large),
                modifier = Modifier
                    .padding(horizontal = Spacing.m)
                    .fillMaxWidth()
                    .clickable(role = Role.Switch) {
                        val on = !settings.overrideEnabled
                        viewModel.setOverrideEnabled(on)
                        if (on && !serviceOn) openAccessibility()
                    },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.override_enabled), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.overrideEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                            checkedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            if (!settings.processingEnabled) {
                NoteCard(Icons.Filled.Info, stringResource(R.string.override_processing_off))
            }
            if (settings.overrideEnabled && !serviceOn) {
                NoteCard(Icons.Filled.Info, stringResource(R.string.override_needs_service)) {
                    Button(onClick = openAccessibility, modifier = Modifier.padding(top = Spacing.s)) {
                        Text(stringResource(R.string.override_open_accessibility))
                    }
                }
            }
            NoteCard(Icons.Filled.Info, stringResource(R.string.override_info) + " " + stringResource(R.string.override_calls_note))

            val active = settings.overrideEnabled
            SwitchRow(stringResource(R.string.override_unlocked), stringResource(R.string.override_unlocked_summary), settings.overrideUnlocked, Icons.Filled.PhoneAndroid, active, viewModel::setOverrideUnlocked)
            ClickRow(stringResource(R.string.override_popup_style), stringResource(settings.popupStyle.label()), Icons.Filled.Palette) { showStyle = true }
            HorizontalDivider(Modifier.padding(vertical = Spacing.xs))
            SwitchRow(stringResource(R.string.override_locked), stateSummary(settings.overrideLocked), settings.overrideLocked, Icons.Filled.Lock, active, viewModel::setOverrideLocked)
            SwitchRow(stringResource(R.string.override_screen_off), stateSummary(settings.overrideScreenOff), settings.overrideScreenOff, Icons.Filled.ScreenLockPortrait, active, viewModel::setOverrideScreenOff)

            SectionHeader(R.string.override_section_tweaks)
            ClickRow(stringResource(R.string.override_step), stringResource(R.string.dial_step_option, settings.overrideStep), Icons.Filled.Adjust) { showStep = true }
            ClickRow(stringResource(R.string.override_popup_side), stringResource(settings.popupSide.label()), Icons.Filled.SwapHoriz) { showSide = true }
        }
    }

    if (showStyle) {
        ChoiceDialog(
            title = stringResource(R.string.override_popup_style),
            options = PopupStyle.entries,
            selected = settings.popupStyle,
            labelOf = { stringResource(it.label()) },
            onSelect = { viewModel.setPopupStyle(it); showStyle = false },
            onDismiss = { showStyle = false },
        )
    }
    if (showStep) {
        ChoiceDialog(
            title = stringResource(R.string.override_step),
            options = OverrideSteps,
            selected = settings.overrideStep,
            labelOf = { stringResource(R.string.dial_step_option, it) },
            onSelect = { viewModel.setOverrideStep(it); showStep = false },
            onDismiss = { showStep = false },
        )
    }
    if (showSide) {
        ChoiceDialog(
            title = stringResource(R.string.override_popup_side),
            options = PopupSide.entries,
            selected = settings.popupSide,
            labelOf = { stringResource(it.label()) },
            onSelect = { viewModel.setPopupSide(it); showSide = false },
            onDismiss = { showSide = false },
        )
    }
}

@Composable
private fun stateSummary(on: Boolean) = stringResource(if (on) R.string.override_on else R.string.override_off)

private fun PopupStyle.label(): Int = when (this) {
    PopupStyle.Accent -> R.string.popup_style_accent
    PopupStyle.Dark -> R.string.popup_style_dark
    PopupStyle.Light -> R.string.popup_style_light
}

private fun PopupSide.label(): Int = when (this) {
    PopupSide.Right -> R.string.popup_side_right
    PopupSide.Left -> R.string.popup_side_left
}

@Composable
private fun NoteCard(icon: ImageVector, text: String, extra: @Composable () -> Unit = {}) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(Radius.medium),
        modifier = Modifier.padding(horizontal = Spacing.m).fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(Spacing.m), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(end = Spacing.m))
            Column {
                Text(text, style = MaterialTheme.typography.bodyLarge)
                extra()
            }
        }
    }
}
