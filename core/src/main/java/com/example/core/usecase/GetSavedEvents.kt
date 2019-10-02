package com.example.core.usecase

import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedEvents @Inject constructor(private val eventsRepository: IEventsRepository) {
    operator fun invoke(): Flow<List<IEvent>> = eventsRepository.getSavedEvents()
}