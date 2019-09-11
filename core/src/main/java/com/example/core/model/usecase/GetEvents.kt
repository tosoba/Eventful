package com.example.core.model.usecase

import com.example.core.IEventsRepository
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import javax.inject.Inject


class GetEvents @Inject constructor(
    private val eventsRepository: IEventsRepository
) {
    suspend operator fun invoke(
        lat: Double, lon: Double, offset: Int?
    ): Resource<PagedResult<IEvent>> = eventsRepository.nearbyEvents(lat, lon, offset)
}