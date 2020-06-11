package com.example.core.repo

import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.model.search.SearchSuggestion
import kotlinx.coroutines.flow.Flow

interface IEventRepository {

    suspend fun getNearbyEvents(
        lat: Double,
        lon: Double,
        offset: Int?
    ): Resource<PagedResult<IEvent>>

    suspend fun searchEvents(searchText: String, offset: Int?): Resource<PagedResult<IEvent>>

    suspend fun getSearchSuggestions(searchText: String): List<SearchSuggestion>

    suspend fun saveSuggestion(searchText: String)

    suspend fun saveEvent(event: IEvent): Boolean

    suspend fun saveEvents(events: List<IEvent>)

    suspend fun deleteEvent(event: IEvent)

    suspend fun deleteEvents(events: List<IEvent>)

    fun getSavedEventsFlow(limit: Int): Flow<List<IEvent>>

    fun isEventSavedFlow(id: String): Flow<Boolean>
}
