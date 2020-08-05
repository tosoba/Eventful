package com.eventful.repo

import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.repo.IAlarmRepository
import com.eventful.db.dao.AlarmDao
import com.eventful.db.dao.EventDao
import com.eventful.db.entity.AlarmEntity
import com.eventful.db.entity.EventAlarmsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val eventsDao: EventDao
) : IAlarmRepository {

    override val alarms: Flow<List<IAlarm>> get() = eventsDao.getAlarms().asAlarmList

    override fun getAlarmsForEvent(eventId: String): Flow<List<IAlarm>> = eventsDao
        .getEventAlarms(eventId)
        .asAlarmList

    private val Flow<List<EventAlarmsEntity>>.asAlarmList: Flow<List<Alarm>>
        get() = map { eventAlarms ->
            eventAlarms.map { (event, alarms) -> alarms.map { Alarm(it.id, event, it.timestamp) } }
                .flatten()
        }

    override suspend fun deleteAlarms(alarmIds: List<Int>) {
        alarmDao.deleteAlarms(alarmIds)
    }

    override suspend fun insertAlarm(eventId: String, timestamp: Long): Int = alarmDao
        .insertAlarm(AlarmEntity(eventId, timestamp))
        .toInt()
}
