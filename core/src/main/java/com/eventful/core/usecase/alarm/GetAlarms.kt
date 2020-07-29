package com.eventful.core.usecase.alarm

import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.repo.IAlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarms @Inject constructor(private val repo: IAlarmRepository) {
    operator fun invoke(eventId: String?): Flow<List<IAlarm>> = if (eventId != null) {
        repo.getAlarmsForEvent(eventId)
    } else {
        repo.alarms
    }
}
