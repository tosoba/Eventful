package com.example.repo

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventsRepository
import com.example.db.dao.SearchSuggestionDao
import com.example.db.entity.SearchSuggestionEntity
import com.example.ticketmasterapi.TicketMasterApi
import com.example.ticketmasterapi.model.EventSearchResponse
import com.example.ticketmasterapi.model.TicketMasterErrorResponse
import com.example.ticketmasterapi.queryparam.GeoPoint
import com.example.ticketmasterapi.queryparam.RadiusUnit
import com.haroldadmin.cnradapter.NetworkResponse

class EventsRepository(
    private val ticketMasterApi: TicketMasterApi,
    private val searchSuggestionDao: SearchSuggestionDao
) : IEventsRepository {

    override suspend fun getNearbyEvents(
        lat: Double, lon: Double, offset: Int?
    ): Resource<PagedResult<IEvent>> = ticketMasterApi.searchEvents(
        radius = DEFAULT_RADIUS,
        radiusUnit = RadiusUnit.KM,
        geoPoint = GeoPoint(lat, lon),
        page = offset
    ).await().asResource

    override suspend fun searchEvents(
        searchText: String
    ): Resource<PagedResult<IEvent>> = ticketMasterApi.searchEvents(searchText)
        .await()
        .asResource

    override suspend fun getSearchSuggestions(
        searchText: String
    ): List<SearchSuggestion> = searchSuggestionDao.getSearchSuggestions(searchText)
        .map { SearchSuggestion(it.id, it.searchText, it.timestampMs) }

    override suspend fun insertSuggestion(searchText: String) {
        searchSuggestionDao.upsertSuggestion(
            SearchSuggestionEntity(searchText, System.currentTimeMillis())
        )
    }

    private val NetworkResponse<EventSearchResponse, TicketMasterErrorResponse>.asResource: Resource<PagedResult<IEvent>>
        get() = when (this) {
            is NetworkResponse.Success -> Resource.Success(
                PagedResult(
                    body.embedded?.events as? List<IEvent> ?: emptyList(),
                    body.page.number,
                    body.page.totalPages
                )
            )
            is NetworkResponse.ServerError -> Resource.Error(this)
            is NetworkResponse.NetworkError -> Resource.Error(error)
        }

    companion object {
        private const val DEFAULT_RADIUS = 10
    }
}