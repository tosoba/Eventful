package com.eventful.core.model.event

interface IVenue {
    val id: String
    val name: String?
    val url: String?
    val address: String?
    val city: String?
    val lat: Double?
    val lng: Double?
}
