package com.precisevolumedial.app.override

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.precisevolumedial.app.audio.AudioStreamType
import com.precisevolumedial.app.audio.VolumeController
import com.precisevolumedial.app.data.PopupSide
import com.precisevolumedial.app.data.PopupStyle
import com.precisevolumedial.app.ui.theme.OnPrimaryDark
import com.precisevolumedial.app.ui.theme.PrimaryDark
import com.precisevolumedial.app.ui.theme.PrimaryLight
import com.precisevolumedial.app.ui.theme.SurfaceDark
import com.precisevolumedial.app.ui.theme.SurfaceHighDark
import com.precisevolumedial.app.ui.theme.SurfaceLight
import com.precisevolumedial.app.ui.theme.TextPrimaryDark
import com.precisevolumedial.app.ui.theme.TextPrimaryLight
import kotlin.math.roundToInt

private data class PopupColors(val panel: Color, val track: Color, val fill: Color, val onFill: Color, val onPanel: Color, val button: Color)

private fun PopupStyle.colors() = when (this) {
    PopupStyle.Accent -> PopupColors(SurfaceDark, SurfaceHighDark, PrimaryDark, OnPrimaryDark, TextPrimaryDark, PrimaryDark)
    PopupStyle.Dark -> PopupColors(SurfaceDark, SurfaceHighDark, Color(0xFFE6EAEE), SurfaceDark, TextPrimaryDark, Color(0xFFE6EAEE))
    PopupStyle.Light -> PopupColors(SurfaceLight, Color(0xFFD8E3EE), PrimaryLight, SurfaceLight, TextPrimaryLight, PrimaryLight)
}

/**
 * Vertical slider pill drawn above every app (accessibility overlay: no extra permission needed).
 * Collapsed it shows the media slider; the "…" button at the bottom expands it to media, ring and alarm.
 * The top button of a column mutes/restores that stream and the track can be dragged;
 * touches outside the pill go to the app.
 */
class VolumePopup(service: AccessibilityService, private val controller: VolumeController) {
    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private val view: PopupView = PopupView(service)
    private var attached = false
    private var side = PopupSide.Right

    private val hideRunnable = Runnable { hide() }

    init {
        view.onMuteTap = { stream -> controller.toggleMute(stream); refresh() }
        view.onDrag = { stream, percent -> controller.setPercent(stream, percent); refresh() }
        view.onToggleExpanded = {
            view.expanded = !view.expanded
            if (attached) windowManager.updateViewLayout(view, layoutParams())
            refresh()
        }
    }

    fun update(style: PopupStyle, side: PopupSide) {
        view.colors = style.colors()
        view.invalidate()
        if (side != this.side) {
            this.side = side
            if (attached) windowManager.updateViewLayout(view, layoutParams())
        }
    }

    /** Shows the popup; [highlight] is the stream that just changed. */
    fun show(highlight: AudioStreamType = AudioStreamType.Media) {
        view.animate().cancel()
        view.alpha = 1f
        refresh()
        if (!attached) {
            attached = runCatching { windowManager.addView(view, layoutParams()) }.isSuccess
        }
        scheduleHide()
    }

    fun hide(immediately: Boolean = false) {
        handler.removeCallbacks(hideRunnable)
        if (!attached) return
        val remove = {
            if (attached) {
                runCatching { windowManager.removeView(view) }
                attached = false
                view.expanded = false
            }
        }
        if (immediately) remove() else view.animate().alpha(0f).setDuration(FadeMs).withEndAction(remove).start()
    }

    private fun refresh() {
        AudioStreamType.entries.forEach { view.percents[it] = controller.read(it).percent }
        view.invalidate()
        scheduleHide()
    }

    private fun scheduleHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, VisibleMs)
    }

    private fun layoutParams() = WindowManager.LayoutParams(
        view.widthPx, view.heightPx,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.CENTER_VERTICAL or (if (side == PopupSide.Right) Gravity.END else Gravity.START)
        x = view.dp(8f).toInt()
    }

    private companion object {
        const val VisibleMs = 2200L
        const val FadeMs = 160L
    }
}

private class PopupView(context: Context) : View(context) {
    var colors = PopupStyle.Accent.colors()
    val percents = mutableMapOf<AudioStreamType, Int>()
    var onMuteTap: (AudioStreamType) -> Unit = {}
    var onDrag: (AudioStreamType, Int) -> Unit = { _, _ -> }
    var onToggleExpanded: () -> Unit = {}
    var expanded = false

    private val pad = dp(6f)
    private val col = dp(52f)
    private val gap = dp(6f)
    private val streams get() = if (expanded) AudioStreamType.entries else listOf(AudioStreamType.Media)

    val widthPx: Int get() = (pad * 2 + col * streams.size + gap * (streams.size - 1)).toInt()
    val heightPx: Int = dp(300f).toInt()

    private val trackTop = pad * 2 + col
    private val trackBottom get() = heightPx - pad * 2 - col
    private var dragStream: AudioStreamType? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rect = RectF()

    fun dp(v: Float): Float = v * resources.displayMetrics.density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = setMeasuredDimension(widthPx, heightPx)

    private fun columnX(index: Int) = pad + index * (col + gap)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val index = ((event.x - pad) / (col + gap)).toInt().coerceIn(0, streams.size - 1)
                val stream = streams[index]
                when {
                    event.y <= trackTop - pad -> onMuteTap(stream)
                    event.y >= trackBottom + pad -> onToggleExpanded()
                    else -> { dragStream = stream; drag(event.y) }
                }
            }
            MotionEvent.ACTION_MOVE -> drag(event.y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragStream = null
        }
        return true
    }

    private fun drag(y: Float) {
        val stream = dragStream ?: return
        onDrag(stream, (100 * (trackBottom - y) / (trackBottom - trackTop)).roundToInt().coerceIn(0, 100))
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        paint.style = Paint.Style.FILL
        paint.color = colors.panel.toArgb()
        canvas.drawRoundRect(0f, 0f, w, height.toFloat(), col / 2 + pad, col / 2 + pad, paint)

        streams.forEachIndexed { i, stream -> drawColumn(canvas, columnX(i), stream, percents[stream] ?: 0) }

        // "…" button at the bottom centre: expands or collapses.
        val cx = w / 2
        val cy = height - pad - col / 2
        paint.color = colors.track.toArgb()
        canvas.drawCircle(cx, cy, col / 2, paint)
        paint.color = colors.onPanel.toArgb()
        for (d in -1..1) canvas.drawCircle(cx + d * dp(8f), cy, dp(2.4f), paint)
    }

    private fun drawColumn(canvas: Canvas, x: Float, stream: AudioStreamType, percent: Int) {
        val cx = x + col / 2

        // Top button with the stream icon.
        paint.style = Paint.Style.FILL
        paint.color = colors.button.toArgb()
        canvas.drawCircle(cx, pad + col / 2, col / 2, paint)
        drawIcon(canvas, stream, cx, pad + col / 2, col * 0.4f, percent == 0)

        // Track and fill.
        rect.set(x, trackTop, x + col, trackBottom)
        paint.style = Paint.Style.FILL
        paint.color = colors.track.toArgb()
        canvas.drawRoundRect(rect, col / 2, col / 2, paint)

        val trackH = trackBottom - trackTop
        val fillH = col + (trackH - col) * (percent.coerceIn(0, 100) / 100f)
        if (percent > 0) {
            val save = canvas.save()
            canvas.clipRect(x, trackBottom - fillH, x + col, trackBottom)
            paint.color = colors.fill.toArgb()
            canvas.drawRoundRect(rect, col / 2, col / 2, paint)
            canvas.restoreToCount(save)
        }

        // Percentage label: on the fill when there is one, otherwise on the track.
        paint.color = (if (percent > 0) colors.onFill else colors.onPanel).toArgb()
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = dp(if (percent >= 100) 16f else 19f)
        val labelY = if (percent > 0) trackBottom - fillH + col / 2 else trackBottom - col / 2
        canvas.drawText(percent.toString(), cx, labelY - (paint.descent() + paint.ascent()) / 2, paint)
    }

    private fun drawIcon(canvas: Canvas, stream: AudioStreamType, cx: Float, cy: Float, size: Float, muted: Boolean) {
        val u = size / 2
        val color = colors.onFill.toArgb()
        paint.color = color
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(1.8f)
        path.reset()
        when (stream) {
            AudioStreamType.Media -> {
                path.moveTo(cx - u, cy - u * 0.4f)
                path.lineTo(cx - u * 0.35f, cy - u * 0.4f)
                path.lineTo(cx + u * 0.3f, cy - u * 0.95f)
                path.lineTo(cx + u * 0.3f, cy + u * 0.95f)
                path.lineTo(cx - u * 0.35f, cy + u * 0.4f)
                path.lineTo(cx - u, cy + u * 0.4f)
                path.close()
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)
                paint.style = Paint.Style.STROKE
                rect.set(cx - u * 0.1f, cy - u * 0.6f, cx + u * 0.9f, cy + u * 0.6f)
                canvas.drawArc(rect, -50f, 100f, false, paint)
            }
            AudioStreamType.Ring -> {
                path.moveTo(cx - u * 0.8f, cy + u * 0.5f)
                path.cubicTo(cx - u * 0.5f, cy + u * 0.2f, cx - u * 0.6f, cy - u * 0.8f, cx, cy - u * 0.8f)
                path.cubicTo(cx + u * 0.6f, cy - u * 0.8f, cx + u * 0.5f, cy + u * 0.2f, cx + u * 0.8f, cy + u * 0.5f)
                path.close()
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)
                canvas.drawCircle(cx, cy + u * 0.82f, u * 0.2f, paint)
            }
            AudioStreamType.Alarm -> {
                paint.style = Paint.Style.STROKE
                canvas.drawCircle(cx, cy + u * 0.1f, u * 0.75f, paint)
                canvas.drawLine(cx, cy + u * 0.1f, cx, cy - u * 0.35f, paint)
                canvas.drawLine(cx, cy + u * 0.1f, cx + u * 0.35f, cy + u * 0.25f, paint)
                canvas.drawLine(cx - u * 0.85f, cy - u * 0.75f, cx - u * 0.4f, cy - u * 0.95f, paint)
                canvas.drawLine(cx + u * 0.85f, cy - u * 0.75f, cx + u * 0.4f, cy - u * 0.95f, paint)
            }
        }
        if (muted) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(2f)
            canvas.drawLine(cx - u, cy - u, cx + u, cy + u, paint)
        }
        paint.style = Paint.Style.FILL
    }
}
