package com.example.core.usecase

import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventsRepository
import javax.inject.Inject

class SaveEvent @Inject constructor(private val eventsRepo: IEventsRepository) {
    suspend operator fun invoke(event: IEvent): Boolean = eventsRepo.saveEvent(event)
}