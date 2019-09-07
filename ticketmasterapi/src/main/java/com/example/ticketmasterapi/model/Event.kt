package com.example.ticketmasterapi.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("_embedded")
    val embedded: EmbeddedAttractionsAndVenues,
    val classifications: List<Classification>,
    val dates: Dates,
    val id: String,
    val images: List<Image>,
    val locale: String,
    val name: String,
    val promoter: Promoter,
    val sales: Sales,
    val test: Boolean,
    val type: String,
    val url: String
)