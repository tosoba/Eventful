package com.eventful.ticketmaster.api.queryparam

enum class RadiusUnit(private val str: String) {
    MILES("miles"),
    KM("km");

    override fun toString(): String = str
}
