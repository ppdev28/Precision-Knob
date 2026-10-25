package com.flatdial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.flatdial.app.R
import com.flatdial.app.ui.theme.Radius
import com.flatdial.app.ui.theme.Spacing

@Composable
fun AppTopBar(
    statusLabel: String,
    processingOn: Boolean,
    onProcessingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val processingLabel = stringResource(R.string.processing_switch)
    Row(
        modifier = modifier.fillMaxWidth().padding(start = Spacing.m, end = Spacing.m, top = Spacing.screenTop, bottom = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(Radius.medium - 4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(modifier = Modifier.padding(start = Spacing.s + Spacing.xs)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Master switch: off = release the audio effects and stop the background service.
            Switch(
                checked = processingOn,
                onCheckedChange = onProcessingChange,
                modifier = Modifier.semantics { contentDescription = processingLabel },
            )
        }
    }
}
