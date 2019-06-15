package com.example.core.model.event

data class EventsResult(
    val events: List<Event>,
    val offset: Int,
    val totalItems: Int
)