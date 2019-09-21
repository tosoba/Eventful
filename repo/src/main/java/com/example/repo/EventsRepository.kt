package com.example.repo

import com.example.core.IEventsRepository
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.ticketmasterapi.TicketMasterApi
import com.example.ticketmasterapi.queryparam.GeoPoint
import com.example.ticketmasterapi.queryparam.RadiusUnit
import com.haroldadmin.cnradapter.NetworkResponse

class EventsRepository(
    private val ticketMasterApi: TicketMasterApi
) : IEventsRepository {

    override suspend fun nearbyEvents(
        lat: Double, lon: Double, offset: Int?
    ): Resource<PagedResult<IEvent>> = when (val response = ticketMasterApi.searchEvents(
        radius = DEFAULT_RADIUS,
        radiusUnit = RadiusUnit.KM,
        geoPoint = GeoPoint(lat, lon),
        page = offset
    ).await()) {
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
    }
}