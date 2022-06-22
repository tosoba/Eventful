package com.eventful.core.android.model.event

import android.os.Parcelable
import androidx.core.text.isDigitsOnly
import com.eventful.core.model.event.IEvent
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

    constructor(
        other: IEvent
    ) : this(
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
        other.attractions?.map { Attraction(it) },
        other.priceRanges?.map { PriceRange(it) })

    val formattedAddress: String
        get() = venues?.firstOrNull()?.run { "$address, $city" } ?: "Unknown address"

    val formattedPriceRange: String
        get() =
            priceRanges?.firstOrNull()?.run {
                if (min.toInt() != max.toInt())
                    "${min.stringNoDecimal} - ${max.stringNoDecimal}$currency"
                else "${min.stringNoDecimal}$currency"
            }
                ?: "Unknown pricing"

    val formattedStartTime: String?
        get() = startTime?.substringBeforeLast(':')

    val formattedStartDate: String
        get() {
            if (startDate == null) return "Unknown start date"
            val calendar = GregorianCalendar.getInstance().apply { time = startDate }
            return "${if (Date().before(startDate)) "Starts on " else "Happened on "} ${String.format(
                "%02d", calendar.get(
                    Calendar.DAY_OF_MONTH
                )
            )}.${String.format(
                "%02d", calendar.get(Calendar.MONTH)
            )}.${String.format(
                "%02d", calendar.get(Calendar.YEAR)
            )}"
        }

    val startDateTimeSetInFuture: Boolean
        get() {
            if (startTime == null || startDate == null) return false
            val splitTime = startTime.split(':')
            if (splitTime.size != 3 || !splitTime.all { it.isDigitsOnly() }) return false
            val calendar = GregorianCalendar.getInstance()
            calendar.time = startDate
            calendar.set(Calendar.HOUR_OF_DAY, splitTime[0].toInt())
            calendar.set(Calendar.MINUTE, splitTime[1].toInt())
            return calendar.timeInMillis > System.currentTimeMillis()
        }

    companion object {
        private val Double.stringNoDecimal: String
            get() = toString().substringBeforeLast(".")
    }
}
