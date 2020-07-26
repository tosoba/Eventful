package com.example.favourites

import com.example.core.util.DataList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.base.SelectableItemsSnackbarState

data class FavouritesState(
    val searchText: String = "",
    override val items: DataList<Selectable<Event>> = DataList(),
    val limit: Int = 0,
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableItemsSnackbarState<FavouritesState, Event> {

    override fun copyWithTransformedItems(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): FavouritesState = copy(items = items.transformItems(transform))

    override fun copyWithSnackbarStateAndTransformedItems(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): FavouritesState = copy(
        items = items.transformItems(transform),
        snackbarState = snackbarState
    )

    override fun copyWithSnackbarState(snackbarState: SnackbarState): FavouritesState = copy(
        snackbarState = snackbarState
    )
}
