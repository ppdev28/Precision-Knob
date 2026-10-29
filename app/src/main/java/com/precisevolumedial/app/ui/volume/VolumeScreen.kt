package com.precisevolumedial.app.ui.volume

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
import com.precisevolumedial.app.R
import com.precisevolumedial.app.audio.AudioStreamType
import com.precisevolumedial.app.data.AppSettings
import com.precisevolumedial.app.ui.AppViewModelProvider
import com.precisevolumedial.app.ui.components.AppTopBar
import com.precisevolumedial.app.ui.components.QuickControls
import com.precisevolumedial.app.ui.components.RotaryDial
import com.precisevolumedial.app.ui.theme.Spacing

@Composable
fun VolumeScreen(
	settings: AppSettings,
	modifier: Modifier = Modifier,
	viewModel: VolumeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val volume = state.volume
	val stream = AudioStreamType.Media
	
	Column(modifier = modifier.fillMaxSize()) {
		AppTopBar(
			processingOn = settings.processingEnabled,
			onProcessingChange = viewModel::setProcessingEnabled,
		)
		
		// The dial takes the remaining space and stays centered.
		Box(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.padding(Spacing.m), contentAlignment = Alignment.Center
		) {
			RotaryDial(
				percent = volume.percent,
				label = if (volume.available) stringResource(stream.dialLabel) else stringResource(R.string.dial_unavailable),
				subLabel = when {
					!volume.available -> null
					volume.fine && volume.trimDb < -0.05f ->
						stringResource(
							R.string.dial_system_step_trim,
							volume.level - volume.min,
							volume.steps,
							volume.trimDb
						)
					
					else -> stringResource(R.string.dial_system_step, volume.level - volume.min, volume.steps)
				},
				showPercent = settings.showPercent,
				enabled = volume.available,
				// No clicks while audio processing is off (the app is "off": no effects, no background service).
				hapticsEnabled = settings.haptics && settings.processingEnabled,
				step = settings.dialStep,
				onPercentChange = viewModel::setPercent,
			)
		}
		
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = Spacing.m, horizontal = Spacing.m),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Box(
				Modifier
					.size(6.dp)
					.background(MaterialTheme.colorScheme.primary, CircleShape)
			)
			Text(
				text = when {
					!settings.processingEnabled -> stringResource(R.string.processing_off_hint)
					volume.available && !volume.fine -> stringResource(R.string.fine_unavailable)
					else -> stringResource(
						R.string.changes_apply_to,
						stringResource(stream.label).lowercase()
					)
				},
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(start = Spacing.s),
			)
		}
		
		QuickControls(
			muted = volume.muted,
			enabled = volume.available,
			onToggleMute = viewModel::toggleMute,
			onDown = viewModel::stepDown,
			onUp = viewModel::stepUp,
		)
	}
}
