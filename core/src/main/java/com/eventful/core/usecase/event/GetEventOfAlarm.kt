package com.eventful.core.usecase.event

import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class GetEventOfAlarm @Inject constructor(private val repo: IEventRepository) {
    suspend operator fun invoke(alarmId: Int): IEvent? = repo.getEventOfAlarm(alarmId)
}
