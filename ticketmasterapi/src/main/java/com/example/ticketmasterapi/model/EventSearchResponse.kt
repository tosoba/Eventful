package com.example.ticketmasterapi.model

import com.google.gson.annotations.SerializedName

data class EventSearchResponse(
    @SerializedName("embedded")
    val embedded: EmbeddedEvents,
    val page: Page
)