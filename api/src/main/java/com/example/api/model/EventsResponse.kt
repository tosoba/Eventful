package com.example.api.model

data class EventsResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<EventApiModel>
)