package com.eventful.core.usecase.event

import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class SaveEvents @Inject constructor(private val repo: IEventRepository) {
    suspend operator fun invoke(events: List<IEvent>) = repo.saveEvents(events)
}
