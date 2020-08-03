package com.eventful.core.android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eventful.core.android.R

object AlarmNotifications {
    private const val ID = "ALARM_CHANNEL_ID"
    private const val NAME = "ALARM_CHANNEL_NAME"
    private const val DESCRIPTION = "ALARM_CHANNEL_DESCRIPTION"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    ID, NAME, NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = DESCRIPTION }
            )
        }
    }

    fun show(context: Context, id: Int) {
        NotificationManagerCompat.from(context)
            .notify(
                id,
                NotificationCompat.Builder(context, ID)
                    .setSmallIcon(R.drawable.alarms)
                    .setContentTitle("My notification")
                    .setContentText("Much longer text that cannot fit one line...")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("Much longer text that cannot fit one line...")
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
            )
    }
}
