package com.eventful.ticketmaster.api.model

import java.util.*

data class Start(
    val dateTBA: Boolean,
    val dateTBD: Boolean,
    val dateTime: Date?,
    val noSpecificTime: Boolean,
    val timeTBA: Boolean,
    val localTime: String?
)
