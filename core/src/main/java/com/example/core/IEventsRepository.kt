package com.example.core

import com.example.core.model.Event

interface IEventsRepository {
    suspend fun getNearbyEvents(lat: Double, lon: Double, offset: Int?): Result<Pair<List<Event>, Int>>
}