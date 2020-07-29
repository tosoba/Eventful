package com.eventful.ticketmaster.model

import com.eventful.core.model.event.IAttraction
import com.eventful.core.model.event.IEvent
import com.eventful.core.model.event.IVenue
import com.eventful.ticketmaster.imageUrl
import com.eventful.ticketmaster.kinds
import com.google.gson.annotations.SerializedName
import java.util.*

data class Event(
    @SerializedName("_embedded")
    val embedded: EmbeddedAttractionsAndVenues,
    val classifications: List<Classification>,
    override val priceRanges: List<PriceRange>?,
    val dates: Dates,
    override val id: String,
    val images: List<Image>,
    val locale: String,
    override val info: String?,
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
    override val kinds: List<String> get() = classifications.kinds
    override val venues: List<IVenue>? get() = embedded.venues
    override val attractions: List<IAttraction>? get() = embedded.attractions
}
