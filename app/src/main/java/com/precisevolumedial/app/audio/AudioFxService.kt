package com.precisevolumedial.app.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.precisevolumedial.app.MainActivity
import com.precisevolumedial.app.R

/** Keeps the process (and therefore the global audio effect) alive while EQ or fine volume is active. */
class AudioFxService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(ChannelId, getString(R.string.fx_channel_name), NotificationManager.IMPORTANCE_MIN),
        )
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.ic_stat_fx)
            .setContentTitle(getString(R.string.fx_notification_title))
            .setContentText(getString(R.string.fx_notification_text))
            .setContentIntent(open)
            .setOngoing(true)
            .build()
        ServiceCompat.startForeground(this, NotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        return START_STICKY
    }

    private companion object {
        const val ChannelId = "audio_fx"
        const val NotificationId = 1
    }
}
