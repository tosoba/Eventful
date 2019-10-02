package com.example.db.entity

import com.example.core.model.ticketmaster.IEvent
import com.example.core.model.ticketmaster.IPriceRange
import java.util.*

data class FullEventEntity(
    val event: EventEntity,
    override val attractions: List<AttractionEntity>,
    override val venues: List<VenueEntity>
) : IEvent {
    override val id: String get() = event.id
    override val name: String get() = event.name
    override val url: String get() = event.url
    override val imageUrl: String get() = event.imageUrl
    override val distance: Float? get() = event.distance
    override val info: String? get() = event.info
    override val salesStartDate: Date? get() = event.salesStartDate
    override val salesEndDate: Date? get() = event.salesEndDate
    override val startDate: Date? get() = event.startDate
    override val startTime: String? get() = event.startTime
    override val kinds: List<String> get() = event.kinds
    override val priceRanges: List<IPriceRange>? get() = event.priceRanges
}