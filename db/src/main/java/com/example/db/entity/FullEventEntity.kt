package com.example.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.core.model.ticketmaster.IEvent
import com.example.core.model.ticketmaster.IPriceRange
import java.util.*

data class FullEventEntity(
    @Embedded val event: EventEntity,

    @Relation(
        parentColumn = "id",
        entity = AttractionEntity::class,
        entityColumn = "id",
        associateBy = Junction(
            value = EventAttractionJoinEntity::class,
            parentColumn = "event_id",
            entityColumn = "attraction_id"
        )
    )
    override val attractions: List<AttractionEntity>?,

    @Relation(
        parentColumn = "id",
        entity = VenueEntity::class,
        entityColumn = "id",
        associateBy = Junction(
            value = EventVenueJoinEntity::class,
            parentColumn = "event_id",
            entityColumn = "venue_id"
        )
    )
    override val venues: List<VenueEntity>?
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
    override val kinds: List<String>? get() = event.kinds
    override val priceRanges: List<IPriceRange>? get() = event.priceRanges
}