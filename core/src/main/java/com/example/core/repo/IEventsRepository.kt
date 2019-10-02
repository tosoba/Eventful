package com.example.core.repo

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import kotlinx.coroutines.flow.Flow

interface IEventsRepository {

    suspend fun getNearbyEvents(
        lat: Double, lon: Double, offset: Int?
    ): Resource<PagedResult<IEvent>>

    suspend fun searchEvents(searchText: String): Resource<PagedResult<IEvent>>

    suspend fun getSearchSuggestions(searchText: String): List<SearchSuggestion>

    suspend fun saveSuggestion(searchText: String)

    suspend fun saveEvent(event: IEvent): Boolean

    fun getSavedEvents(): Flow<List<IEvent>>
}