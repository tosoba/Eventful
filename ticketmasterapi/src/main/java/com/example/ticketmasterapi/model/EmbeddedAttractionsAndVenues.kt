package com.example.ticketmasterapi.model

data class EmbeddedAttractionsAndVenues(
    val attractions: List<Attraction>?,
    val venues: List<Venue>?
)