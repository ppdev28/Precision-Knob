package com.sonicdial.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sonicdial.app.ui.theme.DialUnitStyle
import com.sonicdial.app.ui.theme.DialValueStyle
import com.sonicdial.app.ui.theme.Spacing
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val StartAngle = 135f // 0% sits bottom-left
private const val SweepAngle = 270f // 100% sits bottom-right; the gap is at the bottom
private const val TickCount = 50

/**
 * Rotary potentiometer. The knob follows the finger by angle (drag anywhere on the dial, or tap),
 * clamps at 0/100 instead of wrapping, and reports whole percent values.
 *
 * @param percent value to display (already rounded to the nearest system step by the caller).
 */
@Composable
fun RotaryDial(
    percent: Int,
    label: String,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    showPercent: Boolean = true,
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    onPercentChange: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf(percent / 100f) }
    val shownFraction = if (dragging) dragFraction else percent / 100f

    val knobScale by animateFloatAsState(if (dragging) 1.3f else 1f, label = "knobScale")
    val glowAlpha by animateFloatAsState(if (dragging) 0.35f else 0.18f, label = "glowAlpha")

    fun emit(fraction: Float) {
        dragFraction = fraction
        val newPercent = (fraction * 100).roundToInt()
        if (newPercent != percent) {
            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onPercentChange(newPercent)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 320.dp)
            .aspectRatio(1f)
            .alpha(if (enabled) 1f else 0.38f)
            .semantics {
                stateDescription = "$percent percent"
                progressBarRangeInfo = ProgressBarRangeInfo(percent.toFloat(), 0f..100f, steps = 99)
                if (enabled) setProgress { target -> onPercentChange(target.roundToInt().coerceIn(0, 100)); true }
            }
            .then(
                if (!enabled) Modifier else Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { p ->
                            emit(fractionFromTouch(Offset(size.width / 2f, size.height / 2f), p, percent / 100f))
                        }
                    }
                    .pointerInput(Unit) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        detectDragGestures(
                            onDragStart = { p ->
                                dragging = true
                                dragFraction = fractionFromTouch(center, p, percent / 100f)
                                emit(dragFraction)
                            },
                            onDragEnd = { dragging = false },
                            onDragCancel = { dragging = false },
                        ) { change, _ ->
                            change.consume()
                            emit(fractionFromTouch(center, change.position, dragFraction))
                        }
                    },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val inactive = colors.outline
        val active = colors.primary
        val face = colors.surface
        val faceBorder = colors.outline

        Canvas(modifier = Modifier.matchParentSize()) {
            val c = center
            val outer = size.minDimension / 2f
            val stroke = 14.dp.toPx()
            val tickLen = 8.dp.toPx()
            val tickMajor = 12.dp.toPx()
            val arcRadius = outer - 28.dp.toPx()
            val faceRadius = arcRadius - 24.dp.toPx()
            val arcTopLeft = Offset(c.x - arcRadius, c.y - arcRadius)
            val arcSize = Size(arcRadius * 2, arcRadius * 2)
            val activeSweep = SweepAngle * shownFraction

            // ticks
            for (i in 0..TickCount) {
                val f = i / TickCount.toFloat()
                val angle = Math.toRadians((StartAngle + SweepAngle * f).toDouble())
                val len = if (i % 5 == 0) tickMajor else tickLen
                val on = f <= shownFraction && percent > 0
                drawLine(
                    color = if (on) active else inactive,
                    start = Offset(c.x + (outer - len) * cos(angle).toFloat(), c.y + (outer - len) * sin(angle).toFloat()),
                    end = Offset(c.x + outer * cos(angle).toFloat(), c.y + outer * sin(angle).toFloat()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            // inactive track
            drawArc(inactive, StartAngle, SweepAngle, false, arcTopLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))

            // glow + active arc (hidden at 0% for the muted look)
            if (activeSweep > 0.5f) {
                drawArc(active.copy(alpha = glowAlpha), StartAngle, activeSweep, false, arcTopLeft, arcSize, style = Stroke(stroke * 2f, cap = StrokeCap.Round))
                drawArc(active, StartAngle, activeSweep, false, arcTopLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }

            // knob
            val knobAngle = Math.toRadians((StartAngle + activeSweep).toDouble())
            val knob = Offset(c.x + arcRadius * cos(knobAngle).toFloat(), c.y + arcRadius * sin(knobAngle).toFloat())
            val knobRadius = 11.dp.toPx() * knobScale
            drawCircle(active.copy(alpha = glowAlpha), knobRadius * 1.8f, knob)
            drawCircle(if (percent == 0) inactive else face, knobRadius, knob)
            drawCircle(if (percent == 0) colors.onSurfaceVariant else active, knobRadius, knob, style = Stroke(3.dp.toPx()))

            // knob-less center face
            drawCircle(face, faceRadius, c)
            drawCircle(faceBorder, faceRadius, c, style = Stroke(1.dp.toPx()))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showPercent) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "$percent", style = DialValueStyle, color = colors.onSurface)
                    Text(text = "%", style = DialUnitStyle, color = colors.primary, modifier = Modifier.padding(top = Spacing.s))
                }
            }
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = Spacing.l),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("0", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            Text("100", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}

/** Touch position -> 0..1 along the 270° arc. Touches in the bottom gap snap to the nearer end (no wrap-around). */
private fun fractionFromTouch(center: Offset, touch: Offset, previous: Float): Float {
    var degrees = Math.toDegrees(atan2(touch.y - center.y, touch.x - center.x).toDouble()).toFloat()
    if (degrees < 0) degrees += 360f
    var relative = degrees - StartAngle
    if (relative < 0) relative += 360f
    return if (relative <= SweepAngle) relative / SweepAngle else if (previous > 0.5f) 1f else 0f
}
