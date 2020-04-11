package com.example.favourites

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.DataList
import com.haroldadmin.vector.VectorState

data class FavouritesState(
    val events: DataList<Event>,
    val limit: Int,
    val limitHit: Boolean
) : VectorState {
    companion object {
        val INITIAL: FavouritesState
            get() = FavouritesState(DataList(), 0, false)
    }
}