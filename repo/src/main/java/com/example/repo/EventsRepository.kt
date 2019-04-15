package com.example.repo

import com.example.api.EventsApi
import com.example.api.mapper.core
import com.example.api.model.EventApiModel
import com.example.api.util.EventsArea
import com.example.api.util.EventsRadiusUnit
import com.example.core.IEventsRepository
import com.example.core.Result
import com.example.core.mapSuccess
import com.example.core.model.EventsResult
import com.example.coreandroid.retrofit.awaitResult

class EventsRepository(
    private val api: EventsApi
) : IEventsRepository {

    override suspend fun getNearbyEvents(
        lat: Double, lon: Double, offset: Int?
    ): Result<EventsResult> = api.loadNearbyEvents(
        withinString = EventsArea(DEFAULT_RADIUS, DEFAULT_UNIT, lat, lon).toString(),
        offset = offset
    ).awaitResult().mapSuccess {
        EventsResult(it.results.map(EventApiModel::core), it.results.size + (offset ?: 0), it.count)
    }

    companion object {
        private const val DEFAULT_RADIUS = 10
        private val DEFAULT_UNIT = EventsRadiusUnit.KILOMETERS
    }
}