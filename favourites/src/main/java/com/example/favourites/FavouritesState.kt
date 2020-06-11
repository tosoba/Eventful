package com.example.favourites

import com.example.core.util.DataList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.coreandroid.util.SelectableEventsSnackbarState

data class FavouritesState(
    override val events: DataList<Selectable<Event>> = DataList(),
    val limit: Int = 0,
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableEventsSnackbarState<FavouritesState> {

    override fun copyWithTransformedEvents(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): FavouritesState = copy(events = events.transformItems(transform))

    override fun copyWithSnackbarStateAndTransformedEvents(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): FavouritesState = copy(
        events = events.transformItems(transform),
        snackbarState = snackbarState
    )

    override fun copyWithSnackbarState(snackbarState: SnackbarState): FavouritesState = copy(
        snackbarState = snackbarState
    )
}
