package com.example.core.usecase

import com.example.core.model.event.IEvent
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class SaveEvent @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(event: IEvent): Boolean = eventRepo.saveEvent(event)
}