package com.example.core.model

data class EventsResult(
    val events: List<Event>,
    val offset: Int,
    val totalItems: Int
)