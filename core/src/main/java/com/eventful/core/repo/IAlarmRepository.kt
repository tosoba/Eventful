package com.eventful.core.repo

import com.eventful.core.model.alarm.IAlarm
import kotlinx.coroutines.flow.Flow

interface IAlarmRepository {
    val alarms: Flow<List<IAlarm>>
    fun getAlarmsForEvent(eventId: String): Flow<List<IAlarm>>
    suspend fun deleteAlarms(alarms: List<IAlarm>)
}
