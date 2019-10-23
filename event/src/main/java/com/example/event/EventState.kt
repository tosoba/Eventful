package com.example.event

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.Data
import com.haroldadmin.vector.VectorState

data class EventState(
    val eventArg: Event,
    val isFavourite: Data<Boolean>
) : VectorState