package com.sonicdial.app.ui.components

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
import com.sonicdial.app.audio.eq.EqMaxDb
import com.sonicdial.app.audio.eq.EqMinDb
import com.sonicdial.app.ui.theme.Radius
import com.sonicdial.app.ui.theme.Spacing
import kotlin.math.roundToInt

/** Smooth curve through evenly spaced samples (for log-spaced frequencies this is a response plot). */
@Composable
fun EqCurve(
    gains: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 3f,
    showZeroLine: Boolean = true,
) {
    val zeroColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = modifier) {
        val stepX = size.width / (gains.size - 1)
        val points = gains.mapIndexed { i, g ->
            Offset(i * stepX, size.height * (1f - (g.coerceIn(EqMinDb, EqMaxDb) - EqMinDb) / (EqMaxDb - EqMinDb)))
        }
        if (showZeroLine) drawLine(zeroColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx())
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val cur = points[i]
                val midX = (prev.x + cur.x) / 2f
                cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
            }
        }
        drawPath(path, color, style = Stroke(strokeWidth.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** Vertical band slider, -12..+12 dB, 0 dB at the middle. Drag or tap; exposes a progress semantic for TalkBack. */
@Composable
fun EqBandSlider(
    label: String,
    gainDb: Float,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val trackColor = colors.outline
    val activeColor = if (enabled) colors.primary else colors.onSurfaceVariant
    val dbText = "${if (gainDb > 0) "+" else ""}${gainDb.roundToInt()} dB"

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(dbText, style = MaterialTheme.typography.labelMedium, color = if (gainDb == 0f) colors.onSurfaceVariant else activeColor)
        Canvas(
            modifier = Modifier
                .padding(vertical = Spacing.s)
                .width(Spacing.minTouchTarget)
                .height(200.dp)
                .semantics {
                    contentDescription = label
                    progressBarRangeInfo = ProgressBarRangeInfo(gainDb, EqMinDb..EqMaxDb, steps = 23)
                    if (enabled) setProgress { onGainChange(it.roundToInt().toFloat()); true }
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    fun gainAt(y: Float) = (EqMaxDb - (y / size.height) * (EqMaxDb - EqMinDb))
                        .coerceIn(EqMinDb, EqMaxDb).roundToInt().toFloat()
                    detectTapGestures { onGainChange(gainAt(it.y)) }
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    fun gainAt(y: Float) = (EqMaxDb - (y / size.height) * (EqMaxDb - EqMinDb))
                        .coerceIn(EqMinDb, EqMaxDb).roundToInt().toFloat()
                    detectVerticalDragGestures { change, _ ->
                        change.consume()
                        onGainChange(gainAt(change.position.y))
                    }
                },
        ) {
            val x = size.width / 2
            val mid = size.height / 2
            val thumbY = size.height * (1f - (gainDb - EqMinDb) / (EqMaxDb - EqMinDb))
            drawLine(trackColor, Offset(x, 0f), Offset(x, size.height), 4.dp.toPx(), StrokeCap.Round)
            drawLine(trackColor, Offset(x - 10.dp.toPx(), mid), Offset(x + 10.dp.toPx(), mid), 2.dp.toPx())
            drawLine(activeColor, Offset(x, mid), Offset(x, thumbY), 4.dp.toPx(), StrokeCap.Round)
            drawCircle(colors.surface, 10.dp.toPx(), Offset(x, thumbY))
            drawCircle(activeColor, 10.dp.toPx(), Offset(x, thumbY), style = Stroke(3.dp.toPx()))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
    }
}
