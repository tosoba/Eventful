package com.example.ticketmasterapi.model

data class Start(
    val dateTBA: Boolean,
    val dateTBD: Boolean,
    val localDate: String,
    val noSpecificTime: Boolean,
    val timeTBA: Boolean
)