package com.example.repo

import com.example.core.IEventsRepository
import com.example.core.Result
import com.example.core.mapSuccess
import com.example.core.model.event.EventsResult
import com.example.coreandroid.retrofit.awaitResult
import com.example.eventsapi.EventsApi
import com.example.eventsapi.util.EventsArea
import com.example.eventsapi.util.EventsRadiusUnit

class EventsRepository(private val api: EventsApi) : IEventsRepository {

    override suspend fun getNearbyEvents(
        lat: Double, lon: Double, offset: Int?
    ): Result<EventsResult> = api.loadNearbyEvents(
        withinString = EventsArea(DEFAULT_RADIUS, DEFAULT_UNIT, lat, lon).toString(),
        offset = offset
    ).awaitResult().mapSuccess {
        EventsResult(it.results, it.results.size + (offset ?: 0), it.count)
    }

    companion object {
        private const val DEFAULT_RADIUS = 10
        private val DEFAULT_UNIT = EventsRadiusUnit.KILOMETERS
    }
}