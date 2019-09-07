package com.example.ticketmasterapi.model

data class Venue(
    val address: Address,
    val city: City,
    val country: Country,
    val id: String,
    val locale: String,
    val location: Location,
    val markets: List<Market>,
    val name: String,
    val postalCode: String,
    val state: State,
    val test: Boolean,
    val timezone: String,
    val type: String
)