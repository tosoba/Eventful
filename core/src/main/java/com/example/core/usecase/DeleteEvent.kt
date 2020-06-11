package com.example.core.usecase

import com.example.core.model.event.IEvent
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class DeleteEvent @Inject constructor(private val repo: IEventRepository) {
    suspend operator fun invoke(event: IEvent) = repo.deleteEvent(event)
}
