package com.precisevolumedial.app.audio

import android.media.AudioManager
import androidx.annotation.StringRes
import com.precisevolumedial.app.R

/**
 * The dial and the app screens only control media; the volume button popup also offers ring and alarm
 * (native system steps, no fine control).
 */
enum class AudioStreamType(
    val androidStream: Int,
    @StringRes val label: Int,
    @StringRes val dialLabel: Int,
) {
    Media(AudioManager.STREAM_MUSIC, R.string.stream_media, R.string.dial_label_media),
    Ring(AudioManager.STREAM_RING, R.string.stream_ring, R.string.dial_label_ring),
    Alarm(AudioManager.STREAM_ALARM, R.string.stream_alarm, R.string.dial_label_alarm),
}
