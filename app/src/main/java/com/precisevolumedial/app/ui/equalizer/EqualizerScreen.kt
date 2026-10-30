package com.precisevolumedial.app.ui.equalizer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.precisevolumedial.app.R
import com.precisevolumedial.app.audio.eq.ExternalEq
import com.precisevolumedial.app.audio.eq.logSpaced
import com.precisevolumedial.app.audio.eq.responseDb
import com.precisevolumedial.app.ui.AppViewModelProvider
import com.precisevolumedial.app.ui.components.EqCurve
import com.precisevolumedial.app.ui.theme.Radius
import com.precisevolumedial.app.ui.theme.Spacing
import com.precisevolumedial.app.ui.theme.appSwitchColors
import kotlin.math.roundToInt

@Composable
fun EqualizerScreen(
    onFindProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importFailed by viewModel.importFailed.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val resolver = LocalContext.current.contentResolver
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importFile(it, resolver) }
    }
    val curve = remember(state.filters) { logSpaced(64).map { state.filters.responseDb(it) } }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = Spacing.screenTop, bottom = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        // Header: title, status, master switch
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(stringResource(R.string.eq_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(if (state.enabled) R.string.eq_on else R.string.eq_off),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.enabled) colors.primary else colors.onSurfaceVariant,
                )
            }
            Switch(checked = state.enabled, onCheckedChange = viewModel::setEnabled, colors = appSwitchColors())
        }

        if (!state.fxAvailable) {
            Text(
                stringResource(if (state.processingOn) R.string.eq_fx_unavailable else R.string.eq_processing_off),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.error,
                modifier = Modifier.padding(horizontal = Spacing.m),
            )
        }

        // Headphone correction card
        EqCard {
            Text(stringResource(R.string.eq_headphones), style = MaterialTheme.typography.labelLarge, color = colors.primary)
            val selected = state.selected
            Text(selected?.name ?: stringResource(R.string.eq_no_profile), style = MaterialTheme.typography.titleMedium)
            if (selected != null) {
                Text(
                    stringResource(R.string.eq_profile_summary, selected.source ?: "-", selected.preampDb, selected.filters.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Button(onClick = onFindProfile) { Text(stringResource(R.string.eq_find_profile)) }
                OutlinedButton(onClick = { picker.launch(arrayOf("*/*")) }) { Text(stringResource(R.string.eq_import_file)) }
            }
            if (importFailed) {
                Text(stringResource(R.string.eq_import_failed), color = colors.error, style = MaterialTheme.typography.bodyMedium)
            }
            EqPlot(curve, enabled = state.enabled)

            // Saved profiles
            if (state.profiles.isNotEmpty()) {
                Text(stringResource(R.string.eq_my_profiles), style = MaterialTheme.typography.labelLarge, color = colors.primary)
                state.profiles.forEach { profile ->
                    ListItem(
                        headlineContent = { Text(profile.name) },
                        supportingContent = profile.source?.let { { Text(it) } },
                        leadingContent = { RadioButton(selected = profile.id == state.selectedId, onClick = null) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.delete(profile.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.eq_delete_profile))
                            }
                        },
                        modifier = Modifier.clickable { viewModel.select(profile.id) },
                    )
                }
            }
        }
        ExternalEqSection(
            enabled = state.externalEnabled,
            gains = state.externalGains,
            onEnabledChange = viewModel::setExternalEnabled,
            onGainsChange = viewModel::setExternalGains,
        )
    }
}

private fun bandLabel(hz: Float): String = if (hz >= 1000f) "${(hz / 1000f).toString().removeSuffix(".0")} kHz" else "${hz.toInt()} Hz"

@Composable
private fun presetLabel(key: String): String = stringResource(
    when (key) {
        "bass" -> R.string.anti_eq_preset_bass
        "treble" -> R.string.anti_eq_preset_treble
        "vshape" -> R.string.anti_eq_preset_vshape
        else -> R.string.anti_eq_preset_flat
    },
)

/** Rounded outlined card that groups one feature of the screen. */
@Composable
private fun EqCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Radius.large)
    Column(
        modifier = modifier
            .padding(horizontal = Spacing.m)
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.outline, shape)
            .padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
        content = content,
    )
}

@Composable
private fun EqPlot(
    gains: List<Float>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    referenceGains: List<Float>? = null,
) {
    EqCurve(
        gains = gains,
        referenceGains = referenceGains,
        modifier = modifier.fillMaxWidth().height(112.dp).alpha(if (enabled) 1f else 0.5f).padding(vertical = Spacing.xs),
    )
}

@Composable
private fun LegendDot(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = Spacing.xs))
    }
}

/** Anti-EQ: the user enters the tone settings of the equipment they are connected to; we apply the opposite. */
@Composable
private fun ExternalEqSection(
    enabled: Boolean,
    gains: List<Int>,
    onEnabledChange: (Boolean) -> Unit,
    onGainsChange: (List<Int>) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val correction = remember(gains) {
        val filters = ExternalEq.inverseFilters(gains)
        logSpaced(64).map { filters.responseDb(it) }
    }
    val deviceEffect = remember(correction) { correction.map { -it } }
    EqCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.anti_eq_title), style = MaterialTheme.typography.labelLarge, color = colors.primary)
                Text(stringResource(R.string.anti_eq_summary), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange, colors = appSwitchColors(), modifier = Modifier.padding(start = Spacing.m))
        }
        EqPlot(correction, enabled = enabled, referenceGains = deviceEffect)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            LegendDot(colors.onSurfaceVariant, stringResource(R.string.anti_eq_legend_device))
            LegendDot(colors.primary, stringResource(R.string.anti_eq_legend_correction))
        }
        if (enabled) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                ExternalEq.Presets.forEach { (key, preset) ->
                    FilterChip(
                        selected = gains == preset,
                        onClick = { onGainsChange(preset) },
                        label = { Text(presetLabel(key)) },
                    )
                }
            }
            ExternalEq.BandsHz.forEachIndexed { band, hz ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(bandLabel(hz), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(64.dp))
                    Slider(
                        value = gains[band].toFloat(),
                        onValueChange = { v -> onGainsChange(gains.toMutableList().also { it[band] = v.roundToInt() }) },
                        valueRange = ExternalEq.MinDb.toFloat()..ExternalEq.MaxDb.toFloat(),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(R.string.anti_eq_gain, if (gains[band] > 0) "+${gains[band]}" else "${gains[band]}"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.width(64.dp),
                        textAlign = TextAlign.End,
                    )
                }
            }
            Text(stringResource(R.string.anti_eq_note), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
