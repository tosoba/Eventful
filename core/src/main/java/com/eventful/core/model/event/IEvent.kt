package com.eventful.core.model.event

import java.util.*

interface IEvent {
    val id: String
    val name: String
    val url: String
    val imageUrl: String
    val distance: Float?
    val info: String?
    val salesStartDate: Date?
    val salesEndDate: Date?
    val startDate: Date?
    val startTime: String?
    val kinds: List<String>
    val venues: List<IVenue>?
    val attractions: List<IAttraction>?
    val priceRanges: List<IPriceRange>?
}

val IEvent.startTimestamp: Long
    get() =
        GregorianCalendar.getInstance().run {
            time = requireNotNull(startDate)
            val startTime = requireNotNull(startTime)
            val splitTime = startTime.split(':')
            set(Calendar.HOUR_OF_DAY, splitTime[0].toInt())
            set(Calendar.MINUTE, splitTime[1].toInt())
            return timeInMillis
        }
