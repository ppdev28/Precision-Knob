package com.sonicdial.app.ui.volume

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonicdial.app.R
import com.sonicdial.app.audio.AudioStreamType
import com.sonicdial.app.data.AppSettings
import com.sonicdial.app.ui.AppViewModelProvider
import com.sonicdial.app.ui.components.QuickControls
import com.sonicdial.app.ui.components.RotaryDial
import com.sonicdial.app.ui.components.SonicTopBar
import com.sonicdial.app.ui.components.StreamSelector
import com.sonicdial.app.ui.theme.Spacing

@Composable
fun VolumeScreen(
    settings: AppSettings,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VolumeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val volume = state.volume
    val streamName = stringResource(state.stream.label).lowercase()

    Column(modifier = modifier.fillMaxSize()) {
        SonicTopBar(statusLabel = stringResource(state.stream.statusLabel), onSettings = onOpenSettings)
        StreamSelector(selected = state.stream, onSelect = viewModel::selectStream)

        // The dial takes the remaining space and stays centered.
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(Spacing.m), contentAlignment = Alignment.Center) {
            RotaryDial(
                percent = volume.percent,
                label = if (volume.available) stringResource(state.stream.dialLabel) else stringResource(R.string.dial_unavailable),
                subLabel = when {
                    !volume.available -> null
                    volume.fine && volume.trimDb < -0.05f ->
                        stringResource(R.string.dial_system_step_trim, volume.level - volume.min, volume.steps, volume.trimDb)
                    else -> stringResource(R.string.dial_system_step, volume.level - volume.min, volume.steps)
                },
                showPercent = settings.showPercent,
                enabled = volume.available,
                hapticsEnabled = settings.haptics,
                onPercentChange = viewModel::setPercent,
            )
        }

        QuickControls(
            muted = volume.muted,
            enabled = volume.available,
            onToggleMute = viewModel::toggleMute,
            onDown = viewModel::stepDown,
            onUp = viewModel::stepUp,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.m),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            Text(
                text = if (state.stream == AudioStreamType.Media && volume.available && !volume.fine) {
                    stringResource(R.string.fine_unavailable)
                } else {
                    stringResource(R.string.changes_apply_to, streamName)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.s),
            )
        }
    }
}
