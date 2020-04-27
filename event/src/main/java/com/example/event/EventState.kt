package com.example.event

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.Data

data class EventState(
    val event: Event,
    val isFavourite: Data<Boolean>
)