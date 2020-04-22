package com.example.core.usecase

import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class DeleteEvents @Inject constructor(private val repo: IEventRepository) {
    suspend operator fun invoke(events: List<IEvent>) = repo.deleteEvents(events)
}