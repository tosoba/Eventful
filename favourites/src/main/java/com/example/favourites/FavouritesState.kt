package com.example.favourites

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.SelectableEventsState

data class FavouritesState(
    override val events: DataList<Selectable<Event>> = DataList(),
    val limit: Int = 0,
    val limitHit: Boolean = false
) : SelectableEventsState<FavouritesState> {
    override fun copyWithTransformedEvents(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): FavouritesState = copy(events = events.transformItems(transform))
}