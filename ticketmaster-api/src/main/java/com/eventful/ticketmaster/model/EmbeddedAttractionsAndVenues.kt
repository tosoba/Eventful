package com.eventful.ticketmaster.model

data class EmbeddedAttractionsAndVenues(
    val attractions: List<Attraction>?,
    val venues: List<Venue>?
)
