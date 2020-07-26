package com.example.repo

import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.model.search.SearchSuggestion
import com.example.core.repo.IEventRepository
import com.example.db.dao.EventDao
import com.example.db.dao.SearchSuggestionDao
import com.example.db.entity.SearchSuggestionEntity
import com.example.ticketmasterapi.TicketMasterApi
import com.example.ticketmasterapi.model.EventSearchResponse
import com.example.ticketmasterapi.model.TicketMasterErrorResponse
import com.example.ticketmasterapi.queryparam.GeoPoint
import com.example.ticketmasterapi.queryparam.RadiusUnit
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val ticketMasterApi: TicketMasterApi,
    private val searchSuggestionDao: SearchSuggestionDao,
    private val eventDao: EventDao
) : IEventRepository {

    override suspend fun getNearbyEvents(
        lat: Double,
        lon: Double,
        offset: Int?
    ): Resource<PagedResult<IEvent>> = ticketMasterApi.searchEvents(
        radius = DEFAULT_RADIUS,
        radiusUnit = RadiusUnit.KM,
        geoPoint = GeoPoint(lat, lon),
        page = offset
    ).await().resource

    override suspend fun searchEvents(
        searchText: String,
        offset: Int?
    ): Resource<PagedResult<IEvent>> = ticketMasterApi.searchEvents(
        keyword = searchText,
        page = offset
    ).await().resource

    override suspend fun saveEvent(event: IEvent): Boolean = eventDao.insertEvent(event)

    override suspend fun saveEvents(events: List<IEvent>) = eventDao.insertFullEvents(events)

    override fun getSavedEventsFlow(limit: Int): Flow<List<IEvent>> = eventDao.getEventsFlow(limit)

    override suspend fun deleteEvent(event: IEvent) = eventDao.deleteEvent(event.id)

    override suspend fun deleteEvents(events: List<IEvent>) = eventDao
        .deleteEvents(events.map(IEvent::id))

    override suspend fun getSearchSuggestions(
        searchText: String
    ): List<SearchSuggestion> = searchSuggestionDao.getSearchSuggestions(searchText)
        .map { SearchSuggestion(it.id, it.searchText, it.timestampMs) }

    override suspend fun saveSuggestion(searchText: String) {
        searchSuggestionDao.upsertSuggestion(
            SearchSuggestionEntity(searchText, System.currentTimeMillis())
        )
    }

    override fun isEventSavedFlow(id: String): Flow<Boolean> = eventDao.getEventFlow(id)
        .map { it != null }

    private val NetworkResponse<EventSearchResponse, TicketMasterErrorResponse>.resource: Resource<PagedResult<IEvent>>
        get() = when (this) {
            is NetworkResponse.Success -> Resource.Success(
                PagedResult(
                    body.embedded?.events as? List<IEvent> ?: emptyList(),
                    body.page.number,
                    body.page.totalPages
                )
            )
            is NetworkResponse.ServerError -> Resource.Error(body)
            is NetworkResponse.NetworkError -> Resource.Error(error)
        }

    companion object {
        private const val DEFAULT_RADIUS = 10
    }
}
