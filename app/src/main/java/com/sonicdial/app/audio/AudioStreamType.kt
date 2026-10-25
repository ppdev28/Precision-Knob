package com.sonicdial.app.audio

import android.media.AudioManager
import androidx.annotation.StringRes
import com.sonicdial.app.R

/** The three independent Android streams SonicDial controls. Ring and alarm are NOT media. */
enum class AudioStreamType(
    val androidStream: Int,
    @StringRes val label: Int,
    @StringRes val statusLabel: Int,
    @StringRes val dialLabel: Int,
) {
    Media(AudioManager.STREAM_MUSIC, R.string.stream_media, R.string.status_media_stream, R.string.dial_label_media),
    Ring(AudioManager.STREAM_RING, R.string.stream_ring, R.string.status_ring_stream, R.string.dial_label_ring),
    Alarm(AudioManager.STREAM_ALARM, R.string.stream_alarm, R.string.status_alarm_stream, R.string.dial_label_alarm),
}
