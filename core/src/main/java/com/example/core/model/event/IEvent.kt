package com.example.core.model.event

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

val IEvent.trimmedLowerCasedName: String get() = name.toLowerCase(Locale.getDefault()).trim()
