package com.example.repo

import com.example.core.*
import com.example.core.model.PagedResult
import com.example.core.model.event.EventsResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.retrofit.awaitResponse
import com.example.coreandroid.retrofit.awaitResult
import com.example.eventsapi.EventsApi
import com.example.eventsapi.util.EventsArea
import com.example.eventsapi.util.EventsRadiusUnit
import com.example.ticketmasterapi.TicketMasterApi
import com.example.ticketmasterapi.model.EventSearchResponse
import com.example.ticketmasterapi.model.TicketMasterErrorResponse
import com.example.ticketmasterapi.queryparam.GeoPoint
import com.example.ticketmasterapi.queryparam.RadiusUnit

class EventsRepository(
    private val api: EventsApi,
    private val ticketMasterApi: TicketMasterApi
) : IEventsRepository {

    override suspend fun getNearbyEvents(
        lat: Double, lon: Double, offset: Int?
    ): Result<EventsResult> = api.loadNearbyEvents(
        withinString = EventsArea(DEFAULT_RADIUS, DEFAULT_UNIT, lat, lon).toString(),
        offset = offset
    ).awaitResult().mapSuccess {
        EventsResult(it.results, it.results.size + (offset ?: 0), it.count)
    }

    override suspend fun nearbyEvents(
        lat: Double, lon: Double, offset: Int?
    ): Resource<PagedResult<IEvent>> = when (val response = ticketMasterApi.searchEvents(
        radius = DEFAULT_RADIUS,
        radiusUnit = RadiusUnit.KM,
        geoPoint = GeoPoint(lat, lon)
    ).awaitResponse<EventSearchResponse, TicketMasterErrorResponse>()) {
        is NetworkResponse.Success -> Resource.Success(
            PagedResult(
                response.body.embedded.events as List<IEvent>,
                response.body.page.number,
                response.body.page.totalPages
            )
        )
        is NetworkResponse.ServerError -> Resource.Error(response.body)
        is NetworkResponse.NetworkError -> Resource.Error(response.error)
    }

    companion object {
        private const val DEFAULT_RADIUS = 10
        private val DEFAULT_UNIT = EventsRadiusUnit.KILOMETERS
    }
}