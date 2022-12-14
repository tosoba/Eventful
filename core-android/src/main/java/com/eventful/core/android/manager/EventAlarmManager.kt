package com.eventful.core.android.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eventful.core.android.service.EventAlarmService
import com.eventful.core.manager.IEventAlarmManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventAlarmManager @Inject constructor(private val context: Context) : IEventAlarmManager {

    class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || context == null) return
            val extras = intent.extras
            if (extras == null || !extras.containsKey(EXTRA_ID)) return
            context.startService(EventAlarmService.intent(context, extras.getInt(EXTRA_ID)))
        }

        companion object {
            fun intent(context: Context, alarmId: Int): Intent =
                Intent(context, Receiver::class.java).apply { putExtra(EXTRA_ID, alarmId) }
        }
    }

    private val manager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun create(id: Int, timestamp: Long) {
        create(pendingIntent(id), timestamp)
    }

    override fun cancel(id: Int) {
        manager.cancel(pendingIntent(id))
    }

    override fun update(id: Int, timestamp: Long) {
        val intent = pendingIntent(id)
        manager.cancel(intent)
        create(intent, timestamp)
    }

    private fun pendingIntent(id: Int): PendingIntent =
        PendingIntent.getBroadcast(context, id, Receiver.intent(context, id), 0)

    private fun create(pendingIntent: PendingIntent, timestamp: Long) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ->
                manager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
            else -> manager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
        }
    }

    companion object {
        private const val EXTRA_ID = "EXTRA_ID"
    }
}
