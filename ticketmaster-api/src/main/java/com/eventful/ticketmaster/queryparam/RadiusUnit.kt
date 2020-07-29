package com.eventful.ticketmaster.queryparam

enum class RadiusUnit(private val str: String) {
    MILES("miles"),
    KM("km");

    override fun toString(): String = str
}
