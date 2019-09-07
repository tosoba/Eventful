package com.example.ticketmasterapi.model

data class Attraction(
    val classifications: List<Classification>,
    val id: String,
    val images: List<Image>,
    val locale: String,
    val name: String,
    val test: Boolean,
    val type: String
)