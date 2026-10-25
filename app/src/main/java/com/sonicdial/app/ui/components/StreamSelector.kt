package com.sonicdial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sonicdial.app.audio.AudioStreamType
import com.sonicdial.app.ui.theme.Radius
import com.sonicdial.app.ui.theme.Spacing

@Composable
fun StreamSelector(
    selected: AudioStreamType,
    onSelect: (AudioStreamType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Radius.pill)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.m)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(Spacing.xs)
            .selectableGroup(),
    ) {
        AudioStreamType.entries.forEach { stream ->
            val isSelected = stream == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Spacing.minTouchTarget)
                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent, shape)
                    .then(if (isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape) else Modifier)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(stream) }),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Box(Modifier.size(Spacing.s))
                    }
                    Text(
                        text = stringResource(stream.label),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
