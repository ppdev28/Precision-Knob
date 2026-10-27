package com.flatdial.app.audio

import android.media.AudioManager
import androidx.annotation.StringRes
import com.flatdial.app.R

/** FlatDial only controls the media stream; the enum keeps [VolumeController] stream-agnostic. */
enum class AudioStreamType(
    val androidStream: Int,
    @StringRes val label: Int,
    @StringRes val dialLabel: Int,
) {
    Media(AudioManager.STREAM_MUSIC, R.string.stream_media, R.string.dial_label_media),
}
