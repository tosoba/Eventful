package com.eventful.core.android.service

import android.content.Context
import android.content.Intent
import com.eventful.core.android.notification.AlarmNotifications
import com.eventful.core.android.util.ext.loadBitmap
import com.eventful.core.usecase.alarm.DeleteAlarms
import com.eventful.core.usecase.event.GetEventOfAlarm
import dagger.android.DaggerIntentService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EventAlarmService : DaggerIntentService(NAME) {

    @Inject
    lateinit var getEventOfAlarm: GetEventOfAlarm

    @Inject
    lateinit var deleteAlarm: DeleteAlarms

    @Inject
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onHandleIntent(intent: Intent?) {
        val extras = intent?.extras
        if (extras == null || !extras.containsKey(EXTRA_ID)) return
        val alarmId = extras.getInt(EXTRA_ID)
        GlobalScope.launch {
            val event = withContext(ioDispatcher) { getEventOfAlarm(alarmId) }
            val thumbnail = withContext(ioDispatcher) {
                applicationContext.loadBitmap(event.imageUrl)
            }
            AlarmNotifications.show(applicationContext, alarmId, event, thumbnail)
            withContext(ioDispatcher) { deleteAlarm(listOf(alarmId)) }
        }
    }

    companion object {
        private const val NAME = "ALARM_DELETER_SERVICE"

        private const val EXTRA_ID = "EXTRA_ID"

        fun intent(
            context: Context, alarmId: Int
        ): Intent = Intent(context, EventAlarmService::class.java).apply {
            putExtra(EXTRA_ID, alarmId)
        }
    }
}
