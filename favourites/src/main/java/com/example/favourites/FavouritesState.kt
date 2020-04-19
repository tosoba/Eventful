package com.example.favourites

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.DataList

data class FavouritesState(
    val events: DataList<Event> = DataList(),
    val limit: Int = 0,
    val limitHit: Boolean = false
)