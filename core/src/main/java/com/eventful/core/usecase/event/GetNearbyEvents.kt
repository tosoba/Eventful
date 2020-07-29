package com.eventful.core.usecase.event

import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class GetNearbyEvents @Inject constructor(private val eventRepository: IEventRepository) {
    suspend operator fun invoke(
        lat: Double,
        lon: Double,
        offset: Int?
    ): Resource<PagedResult<IEvent>> = eventRepository.getNearbyEvents(lat, lon, offset)
}
