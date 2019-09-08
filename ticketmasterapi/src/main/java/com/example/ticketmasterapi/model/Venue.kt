package com.example.ticketmasterapi.model

import com.example.core.model.ticketmaster.IVenue
import com.google.gson.annotations.SerializedName

data class Venue(
    @SerializedName("address")
    val _address: Address,
    @SerializedName("city")
    val _city: City,
    val country: Country,
    override val id: String,
    override val url: String,
    val locale: String,
    val location: Location,
    val markets: List<Market>,
    override val name: String,
    val postalCode: String,
    val state: State,
    val timezone: String,
    val type: String
) : IVenue {
    override val address: String get() = _address.line1
    override val city: String get() = _city.name
    override val lat: Float get() = location.latitude.toFloat()
    override val lng: Float get() = location.longitude.toFloat()
}