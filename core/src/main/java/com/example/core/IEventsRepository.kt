package com.example.core

import com.example.core.model.PagedResult
import com.example.core.model.event.EventsResult
import com.example.core.model.ticketmaster.IEvent

interface IEventsRepository {
    suspend fun getNearbyEvents(lat: Double, lon: Double, offset: Int?): Result<EventsResult>
    suspend fun nearbyEvents(lat: Double, lon: Double, offset: Int?): Resource<PagedResult<IEvent>>
}