package com.eventful.ticketmaster.api.model

data class Classification(
    val genre: Genre?,
    val primary: Boolean?,
    val segment: Segment?,
    val subGenre: SubGenre?
)
