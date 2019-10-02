package com.example.core.usecase

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventsRepository
import javax.inject.Inject

class GetNearbyEvents @Inject constructor(private val eventsRepository: IEventsRepository) {
    suspend operator fun invoke(
        lat: Double, lon: Double, offset: Int?
    ): Resource<PagedResult<IEvent>> = eventsRepository.getNearbyEvents(lat, lon, offset)
}