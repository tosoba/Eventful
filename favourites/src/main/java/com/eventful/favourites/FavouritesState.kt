package com.eventful.favourites

import com.eventful.core.util.DataList
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable
import com.eventful.core.android.base.SelectableItemsSnackbarState

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
