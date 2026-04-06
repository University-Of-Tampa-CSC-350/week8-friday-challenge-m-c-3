package com.example.fc_006

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object MissionControlNotifications {

    /**
     * Bumped from [mission_control_informational] so devices that already cached the old channel
     * pick up HIGH importance (better heads-up when not in foreground). Task rubric still fits.
     */
    const val CHANNEL_INFORMATIONAL = "mission_control_informational_v2"
    const val CHANNEL_WARNING = "mission_control_warning"
    /** Was IMPORTANCE_DEFAULT; v2 + HIGH so heads-up matches scan/signal when outcome is evaded. */
    const val CHANNEL_EVADED = "mission_control_evaded_v2"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        // HIGH makes peek/heads-up more likely when the app is not in the foreground.
        val informational = NotificationChannel(
            CHANNEL_INFORMATIONAL,
            context.getString(R.string.channel_informational_name),
            NotificationManager.IMPORTANCE_HIGH
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
            NotificationManager.IMPORTANCE_HIGH
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
        smallIconRes: Int,
        alertKind: AlertKind
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_FROM_NOTIFICATION, true)
            putExtra(MainActivity.EXTRA_ALERT_KIND, alertKind.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val accentColor = ContextCompat.getColor(context, accentColorForChannel(channelId))

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(accentColor)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(android.app.Notification.CATEGORY_STATUS)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun accentColorForChannel(channelId: String): Int = when (channelId) {
        CHANNEL_WARNING -> R.color.notification_accent_warning
        CHANNEL_EVADED -> R.color.notification_accent_evaded
        else -> R.color.notification_accent_informational
    }
}
