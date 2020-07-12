package com.example.coreandroid.base

import com.example.core.util.HoldsList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.google.android.material.snackbar.Snackbar

interface SelectableEventsState<S : SelectableEventsState<S>> {
    val events: HoldsList<Selectable<Event>>
    fun copyWithTransformedEvents(transform: (Selectable<Event>) -> Selectable<Event>): S
}

interface SelectableEventsSnackbarState<S : SelectableEventsSnackbarState<S>> :
    SelectableEventsState<S>,
    HoldsSnackbarState<S> {
    fun copyWithSnackbarStateAndTransformedEvents(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): S
}

interface HoldsSnackbarState<S> {
    fun copyWithSnackbarState(snackbarState: SnackbarState): S
}

interface ClearSelectionUpdate<S : SelectableEventsState<S>> : StateUpdate<S> {
    override fun invoke(state: S): S = state.copyWithTransformedEvents { it.copy(selected = false) }
}

interface ToggleEventSelectionUpdate<S : SelectableEventsState<S>> : StateUpdate<S> {
    val event: Event
    override fun invoke(state: S): S = state.copyWithTransformedEvents {
        if (it.item.id == event.id) Selectable(event, !it.selected) else it
    }
}

interface EventSelectionConfirmedUpdate<S : SelectableEventsSnackbarState<S>> : StateUpdate<S> {
    val snackbarText: String
    val onSnackbarDismissed: () -> Unit
    override fun invoke(state: S): S = state.copyWithSnackbarStateAndTransformedEvents(
        snackbarState = SnackbarState.Shown(
            text = snackbarText,
            length = Snackbar.LENGTH_SHORT,
            onDismissed = onSnackbarDismissed
        )
    ) { event -> event.copy(selected = false) }
}

fun addedToFavouritesMessage(eventsCount: Int): String =
    """$eventsCount
            |${if (eventsCount > 1) " events were" else " event was"} 
            |added to favourites""".trimMargin().replace("\n", "")

fun removedFromFavouritesMessage(eventsCount: Int): String =
    """$eventsCount
            |${if (eventsCount > 1) " events were" else " event was"} 
            |removed from favourites""".trimMargin().replace("\n", "")