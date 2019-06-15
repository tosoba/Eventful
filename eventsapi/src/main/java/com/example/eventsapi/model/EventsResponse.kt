package com.example.eventsapi.model

import com.example.core.model.event.Event

data class EventsResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Event>
)