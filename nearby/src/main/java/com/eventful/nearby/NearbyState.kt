package com.eventful.nearby

import com.eventful.core.android.base.SelectableItemsSnackbarState
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable
import com.eventful.core.util.PagedDataList

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
    ): NearbyState = copy(items = items.transformItems(transform), snackbarState = snackbarState)

    override fun copyWithSnackbarState(snackbarState: SnackbarState): NearbyState =
        copy(snackbarState = snackbarState)
}
