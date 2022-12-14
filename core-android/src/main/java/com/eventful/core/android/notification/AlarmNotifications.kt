package com.eventful.core.android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eventful.core.android.R
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.ext.printedNormalized
import com.eventful.core.model.event.IEvent
import com.eventful.core.model.event.startTimestamp
import org.joda.time.Period

object AlarmNotifications {
    private const val ID = "ALARM_CHANNEL_ID"
    private const val NAME = "ALARM_CHANNEL_NAME"
    private const val DESCRIPTION = "ALARM_CHANNEL_DESCRIPTION"
    const val EVENT_EXTRA = "EVENT_EXTRA"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context
                .getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(
                    NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = DESCRIPTION
                    })
        }
    }

    fun show(
        context: Context,
        alarmId: Int,
        event: IEvent,
        thumbnail: Bitmap?,
        contentIntent: Intent
    ) {
        val summaryText =
            "Starts in: ${Period.seconds(((event.startTimestamp - System.currentTimeMillis()) / 1000L).toInt()).printedNormalized}"
        NotificationManagerCompat.from(context)
            .notify(
                alarmId,
                NotificationCompat.Builder(context, ID)
                    .setSmallIcon(R.drawable.alarms)
                    .setContentTitle(event.name)
                    .setContentText(event.name)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            0,
                            contentIntent.apply { putExtra(EVENT_EXTRA, Event(event)) },
                            0))
                    .setStyle(
                        if (thumbnail != null) {
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(thumbnail)
                                .setBigContentTitle(event.name)
                                .setSummaryText(summaryText)
                        } else {
                            NotificationCompat.BigTextStyle()
                                .setBigContentTitle(event.name)
                                .setSummaryText(summaryText)
                        })
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build())
    }
}
