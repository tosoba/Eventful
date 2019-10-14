package com.example.core.usecase

import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedEvents @Inject constructor(private val eventRepository: IEventRepository) {
    operator fun invoke(limit: Int): Flow<List<IEvent>> = eventRepository.getSavedEventsFlow(limit)
}