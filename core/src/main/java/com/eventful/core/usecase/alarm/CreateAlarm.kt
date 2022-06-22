package com.eventful.core.usecase.alarm

import com.eventful.core.manager.IEventAlarmManager
import com.eventful.core.repo.IAlarmRepository
import javax.inject.Inject

class CreateAlarm
@Inject
constructor(private val repo: IAlarmRepository, private val manager: IEventAlarmManager) {
    suspend operator fun invoke(eventId: String, timestamp: Long) {
        val id = repo.insertAlarm(eventId, timestamp)
        manager.create(id, timestamp)
    }
}
