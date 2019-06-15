package com.example.eventsapi.util

class EventsArea(
    private val radius: Int,
    private val unit: EventsRadiusUnit,
    private val latitude: Double,
    private val longitude: Double
) {
    override fun toString(): String = "$radius${unit.symbol}@$latitude,$longitude"
}
