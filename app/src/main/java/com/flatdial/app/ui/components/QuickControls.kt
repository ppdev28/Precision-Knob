package com.flatdial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.flatdial.app.R
import com.flatdial.app.ui.theme.Spacing

@Composable
fun QuickControls(
    muted: Boolean,
    enabled: Boolean,
    onToggleMute: () -> Unit,
    onDown: () -> Unit,
    onUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.l),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuickAction(
            icon = if (muted) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
            label = stringResource(if (muted) R.string.action_unmute else R.string.action_mute),
            accent = false,
            enabled = enabled,
            onClick = onToggleMute,
        )
        QuickAction(Icons.Filled.Remove, stringResource(R.string.action_down), accent = true, enabled = enabled, onClick = onDown)
        QuickAction(Icons.Filled.Add, stringResource(R.string.action_up), accent = true, enabled = enabled, onClick = onUp)
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    accent: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(if (enabled) 1f else 0.38f)) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(56.dp) // >= 48dp touch target
                .clip(CircleShape)
                .background(if (accent) colors.primaryContainer else colors.surface)
                .border(1.dp, colors.outline, CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (accent) colors.primary else colors.onSurface)
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.xs))
    }
}
