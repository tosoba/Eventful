package com.example.ticketmasterapi.model

import java.util.*

data class Start(
    val dateTBA: Boolean,
    val dateTBD: Boolean,
    val dateTime: Date?, // TODO: Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    val noSpecificTime: Boolean,
    val timeTBA: Boolean,
    val localTime: String?
)
