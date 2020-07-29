package com.eventful.core.usecase.event

import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class SaveEvent @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(event: IEvent): Boolean = eventRepo.saveEvent(event)
}
