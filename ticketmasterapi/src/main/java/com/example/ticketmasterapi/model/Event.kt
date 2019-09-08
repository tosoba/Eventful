package com.example.ticketmasterapi.model

import com.example.core.model.ticketmaster.IAttraction
import com.example.core.model.ticketmaster.IEvent
import com.example.core.model.ticketmaster.IVenue
import com.example.ticketmasterapi.imageUrl
import com.example.ticketmasterapi.kind
import com.google.gson.annotations.SerializedName
import java.util.*

data class Event(
    @SerializedName("_embedded")
    val embedded: EmbeddedAttractionsAndVenues,
    val classifications: List<Classification>,
    val dates: Dates,
    override val id: String,
    val images: List<Image>,
    val locale: String,
    override val name: String,
    override val distance: Float?,
    val sales: Sales,
    val type: String,
    override val url: String
) : IEvent {
    override val imageUrl: String get() = images.imageUrl
    override val salesStartDate: Date? get() = sales.public.startDateTime
    override val salesEndDate: Date? get() = sales.public.endDateTime
    override val startDate: Date? get() = dates.start.dateTime
    override val startTime: String? get() = dates.start.localTime
    override val kind: String? get() = classifications.kind
    override val venues: List<IVenue> get() = embedded.venues
    override val attractions: List<IAttraction> get() = embedded.attractions
}