package com.example.nearby

import com.example.core.util.PagedDataList
import com.example.coreandroid.base.SelectableItemsSnackbarState
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable

data class NearbyState(
    override val items: PagedDataList<Selectable<Event>> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableItemsSnackbarState<NearbyState, Event> {

    override fun copyWithTransformedItems(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): NearbyState = copy(items = items.transformItems(transform))

    override fun copyWithSnackbarStateAndTransformedItems(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): NearbyState = copy(
        items = items.transformItems(transform),
        snackbarState = snackbarState
    )

    override fun copyWithSnackbarState(snackbarState: SnackbarState): NearbyState = copy(
        snackbarState = snackbarState
    )
}
