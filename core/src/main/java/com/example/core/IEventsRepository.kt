package com.example.core

import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent

interface IEventsRepository {
    suspend fun nearbyEvents(lat: Double, lon: Double, offset: Int?): Resource<PagedResult<IEvent>>
    suspend fun searchEvents(searchText: String): Resource<PagedResult<IEvent>>
}