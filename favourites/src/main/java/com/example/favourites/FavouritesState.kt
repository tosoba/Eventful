package com.example.favourites

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.PagedDataList
import com.haroldadmin.vector.VectorState

data class FavouritesState(val events: PagedDataList<Event>) : VectorState {
    companion object {
        val INITIAL: FavouritesState
            get() = FavouritesState(PagedDataList())
    }
}