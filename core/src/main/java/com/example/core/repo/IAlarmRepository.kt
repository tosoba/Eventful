package com.example.core.repo

import com.example.core.model.alarm.IAlarm
import kotlinx.coroutines.flow.Flow

interface IAlarmRepository {
    val alarms: Flow<List<IAlarm>>
    fun getAlarmsForEvent(eventId: String): Flow<List<IAlarm>>
    suspend fun deleteAlarms(alarms: List<IAlarm>)
}
