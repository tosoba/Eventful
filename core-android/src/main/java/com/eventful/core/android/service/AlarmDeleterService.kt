package com.eventful.core.android.service

import android.content.Context
import android.content.Intent
import com.eventful.core.usecase.alarm.DeleteAlarms
import dagger.android.DaggerIntentService
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class AlarmDeleterService : DaggerIntentService(NAME), CoroutineScope {
    private val supervisorJob: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisorJob

    @Inject
    lateinit var deleteAlarm: DeleteAlarms

    override fun onHandleIntent(intent: Intent?) {
        val extras = intent?.extras
        if (extras == null || !extras.containsKey(EXTRA_ID)) return
        launch {
            deleteAlarm(listOf(extras.getInt(EXTRA_ID)))
        }
    }

    override fun onDestroy() {
        supervisorJob.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NAME = "ALARM_DELETER_SERVICE"

        private const val EXTRA_ID = "EXTRA_ID"

        fun intent(
            context: Context, alarmId: Int
        ): Intent = Intent(context, AlarmDeleterService::class.java).apply {
            putExtra(EXTRA_ID, alarmId)
        }
    }
}
