package com.eventful.core.repo

import com.eventful.core.model.alarm.IAlarm
import kotlinx.coroutines.flow.Flow

interface IAlarmRepository {
    val alarms: Flow<List<IAlarm>>
    fun getAlarmsForEvent(eventId: String): Flow<List<IAlarm>>
    fun getUpcomingAlarms(limit: Int): Flow<List<IAlarm>>
    suspend fun deleteAlarms(alarmIds: List<Int>)
    suspend fun insertAlarm(eventId: String, timestamp: Long): Int
    suspend fun updateAlarm(id: Int, timestamp: Long)
}
