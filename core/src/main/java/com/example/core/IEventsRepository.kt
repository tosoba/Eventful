package com.example.core

import com.example.core.model.event.EventsResult

interface IEventsRepository {
    suspend fun getNearbyEvents(lat: Double, lon: Double, offset: Int?): Result<EventsResult>
}