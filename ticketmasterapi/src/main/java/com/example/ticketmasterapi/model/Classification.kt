package com.example.ticketmasterapi.model

data class Classification(
    val genre: Genre,
    val primary: Boolean,
    val segment: Segment,
    val subGenre: SubGenre?
)