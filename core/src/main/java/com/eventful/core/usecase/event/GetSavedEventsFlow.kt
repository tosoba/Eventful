package com.eventful.core.usecase.event

import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedEventsFlow @Inject constructor(private val eventRepository: IEventRepository) {
    operator fun invoke(limit: Int): Flow<List<IEvent>> = eventRepository.getSavedEventsFlow(limit)
}
