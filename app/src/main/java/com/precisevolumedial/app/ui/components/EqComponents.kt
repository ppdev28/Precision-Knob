package com.precisevolumedial.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.precisevolumedial.app.audio.eq.EqMaxDb
import com.precisevolumedial.app.audio.eq.EqMinDb
import com.precisevolumedial.app.ui.theme.Radius
import com.precisevolumedial.app.ui.theme.Spacing
import kotlin.math.roundToInt

/**
 * Smooth curve through evenly spaced samples (for log-spaced frequencies this is a response plot).
 * [referenceGains] is an optional second curve drawn underneath in [referenceColor] (e.g. the effect to cancel).
 */
@Composable
fun EqCurve(
    gains: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 3f,
    showZeroLine: Boolean = true,
    referenceGains: List<Float>? = null,
    referenceColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val zeroColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = modifier) {
        if (showZeroLine) drawLine(zeroColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx())
        referenceGains?.let { drawGains(it, referenceColor, strokeWidth * 0.6f, dashed = true) }
        drawGains(gains, color, strokeWidth)
    }
}

private fun DrawScope.drawGains(gains: List<Float>, color: Color, strokeWidth: Float, dashed: Boolean = false) {
    val stepX = size.width / (gains.size - 1)
    val points = gains.mapIndexed { i, g ->
        Offset(i * stepX, size.height * (1f - (g.coerceIn(EqMinDb, EqMaxDb) - EqMinDb) / (EqMaxDb - EqMinDb)))
    }
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val cur = points[i]
            val midX = (prev.x + cur.x) / 2f
            cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
        }
    }
    val effect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())) else null
    drawPath(path, color, style = Stroke(strokeWidth.dp.toPx(), cap = StrokeCap.Round, pathEffect = effect))
}
