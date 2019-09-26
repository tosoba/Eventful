package com.example.core.model.ticketmaster

interface IVenue {
    val id: String
    val name: String
    val url: String?
    val address: String?
    val city: String
    val lat: Float
    val lng: Float
}