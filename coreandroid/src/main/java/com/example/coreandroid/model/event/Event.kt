package com.example.coreandroid.model.event

import android.os.Parcelable
import com.example.core.model.event.IEvent
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Event(
    override val id: String,
    override val name: String,
    override val url: String,
    override val imageUrl: String,
    override val distance: Float?,
    override val info: String?,
    override val salesStartDate: Date?,
    override val salesEndDate: Date?,
    override val startDate: Date?,
    override val startTime: String?,
    override val kinds: List<String>,
    override val venues: List<Venue>?,
    override val attractions: List<Attraction>?,
    override val priceRanges: List<PriceRange>?
) : IEvent, Parcelable {

    constructor(other: IEvent) : this(
        other.id,
        other.name,
        other.url,
        other.imageUrl,
        other.distance,
        other.info,
        other.salesStartDate,
        other.salesEndDate,
        other.startDate,
        other.startTime,
        other.kinds,
        other.venues?.map { Venue(it) },
        other.attractions?.map {
            Attraction(
                it
            )
        },
        other.priceRanges?.map {
            PriceRange(
                it
            )
        }
    )

    val formattedAddress: String
        get() = venues?.firstOrNull()?.run { "$address, $city" } ?: "Unknown address"

    val formattedPriceRange: String
        get() = priceRanges?.firstOrNull()?.run {
            if (min.toInt() != max.toInt()) "${min.stringNoDecimal} - ${max.stringNoDecimal}$currency"
            else "${min.stringNoDecimal}$currency"
        } ?: "Unknown pricing"

    val formattedStartTime: String?
        get() = startTime?.substringBeforeLast(':')

    companion object {
        private val Double.stringNoDecimal: String
            get() = toString().substringBeforeLast(".")
    }
}

data class Selectable<T>(val item: T, val selected: Boolean = false)
