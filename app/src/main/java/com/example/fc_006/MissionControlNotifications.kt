package com.example.fc_006

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object MissionControlNotifications {

    const val CHANNEL_INFORMATIONAL = "mission_control_informational"
    const val CHANNEL_WARNING = "mission_control_warning"
    const val CHANNEL_EVADED = "mission_control_evaded"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val informational = NotificationChannel(
            CHANNEL_INFORMATIONAL,
            context.getString(R.string.channel_informational_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_informational_description)
        }

        val warning = NotificationChannel(
            CHANNEL_WARNING,
            context.getString(R.string.channel_warning_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_warning_description)
            enableVibration(true)
        }

        val evaded = NotificationChannel(
            CHANNEL_EVADED,
            context.getString(R.string.channel_evaded_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_evaded_description)
        }

        manager.createNotificationChannel(informational)
        manager.createNotificationChannel(warning)
        manager.createNotificationChannel(evaded)
    }

    fun show(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        smallIconRes: Int
    ) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
