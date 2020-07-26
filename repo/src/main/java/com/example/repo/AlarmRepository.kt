package com.example.repo

import com.example.core.model.alarm.IAlarm
import com.example.core.repo.IAlarmRepository
import com.example.coreandroid.model.alarm.Alarm
import com.example.db.dao.AlarmDao
import com.example.db.dao.EventDao
import com.example.db.entity.EventAlarmsEntity
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

    override suspend fun deleteAlarms(alarms: List<IAlarm>) {
        alarmDao.deleteAlarms(alarms.map(IAlarm::id))
    }
}
