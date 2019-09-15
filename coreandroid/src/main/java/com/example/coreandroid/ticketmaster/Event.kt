package com.example.coreandroid.ticketmaster

import android.os.Parcelable
import com.example.core.model.ticketmaster.IEvent
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Event(
    override val id: String,
    override val name: String,
    override val url: String,
    override val imageUrl: String,
    override val distance: Float?,
    override val salesStartDate: Date?,
    override val salesEndDate: Date?,
    override val startDate: Date?,
    override val startTime: String?,
    override val kind: String?,
    override val venues: List<Venue>,
    override val attractions: List<Attraction>
) : IEvent, Parcelable {
    constructor(other: IEvent) : this(
        other.id,
        other.name,
        other.url,
        other.imageUrl,
        other.distance,
        other.salesStartDate,
        other.salesEndDate,
        other.startDate,
        other.startTime,
        other.kind,
        other.venues.map { Venue(it) },
        other.attractions.map { Attraction(it) }
    )
}