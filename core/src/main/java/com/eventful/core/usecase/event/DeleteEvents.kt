package com.eventful.core.usecase.event

import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class DeleteEvents @Inject constructor(private val repo: IEventRepository) {
    suspend operator fun invoke(events: List<IEvent>) = repo.deleteEvents(events)
}
