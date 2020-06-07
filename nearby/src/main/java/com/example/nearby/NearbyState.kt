package com.example.nearby

import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.core.util.PagedDataList
import com.example.coreandroid.util.SelectableEventsSnackbarState

data class NearbyState(
    override val events: PagedDataList<Selectable<Event>> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableEventsSnackbarState<NearbyState> {

    override fun copyWithTransformedEvents(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): NearbyState = copy(events = events.transformItems(transform))

    override fun copyWithSnackbarStateAndTransformedEvents(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): NearbyState = copy(
        events = events.transformItems(transform),
        snackbarState = snackbarState
    )

    override fun copyWithSnackbarState(snackbarState: SnackbarState): NearbyState = copy(
        snackbarState = snackbarState
    )
}
