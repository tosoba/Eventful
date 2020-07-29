package com.eventful.ticketmaster.api.model

data class EmbeddedAttractionsAndVenues(
    val attractions: List<Attraction>?,
    val venues: List<Venue>?
)
