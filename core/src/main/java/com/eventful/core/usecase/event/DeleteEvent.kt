package com.eventful.core.usecase.event

import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class DeleteEvent @Inject constructor(private val repo: IEventRepository) {
    suspend operator fun invoke(event: IEvent) = repo.deleteEvent(event)
}
