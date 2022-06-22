package com.eventful.ticketmaster.api.model

import com.google.gson.annotations.SerializedName

data class EventSearchResponse(
    @SerializedName("_embedded") val embedded: EmbeddedEvents?,
    val page: Page
)
