package com.example.core.usecase

import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class SaveEvents @Inject constructor(private val repo: IEventRepository) {
    suspend operator fun invoke(events: List<IEvent>) = repo.saveEvents(events)
}