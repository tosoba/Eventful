package com.example.coreandroid.base

import com.example.core.util.HoldsList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Selectable
import com.google.android.material.snackbar.Snackbar

interface SelectableItemsState<S : SelectableItemsState<S, I>, I> {
    val items: HoldsList<Selectable<I>>
    fun copyWithTransformedItems(transform: (Selectable<I>) -> Selectable<I>): S
}

interface SelectableItemsSnackbarState<S : SelectableItemsSnackbarState<S, I>, I> :
    SelectableItemsState<S, I>,
    HoldsSnackbarState<S> {
    fun copyWithSnackbarStateAndTransformedItems(
        snackbarState: SnackbarState,
        transform: (Selectable<I>) -> Selectable<I>
    ): S
}

interface HoldsSnackbarState<S> {
    fun copyWithSnackbarState(snackbarState: SnackbarState): S
}

interface ClearSelectionUpdate<S : SelectableItemsState<S, I>, I> : StateUpdate<S> {
    override fun invoke(state: S): S = state.copyWithTransformedItems { it.copy(selected = false) }
}

interface ToggleItemSelectionUpdate<S : SelectableItemsState<S, I>, I, ID> : StateUpdate<S> {
    val item: I
    fun I.id(): ID

    override fun invoke(state: S): S = state.copyWithTransformedItems {
        if (it.item.id() == item.id()) Selectable(item, !it.selected) else it
    }
}

interface ItemSelectionConfirmedUpdate<S : SelectableItemsSnackbarState<S, I>, I> : StateUpdate<S> {
    val snackbarText: String
    val onSnackbarDismissed: () -> Unit

    override fun invoke(state: S): S = state.copyWithSnackbarStateAndTransformedItems(
        snackbarState = SnackbarState.Shown(
            text = snackbarText,
            length = Snackbar.LENGTH_SHORT,
            onDismissed = onSnackbarDismissed
        )
    ) { selectable ->
        selectable.copy(selected = false)
    }
}

fun addedToAlarmsMessage(alarmsCount: Int): String =
    """$alarmsCount
            |${if (alarmsCount > 1) " alarms were" else " event was"} 
            |added.""".trimMargin().replace("\n", "")

fun removedFromAlarmsMessage(alarmsCount: Int): String =
    """$alarmsCount
            |${if (alarmsCount > 1) " alarms were" else " event was"} 
            |removed.""".trimMargin().replace("\n", "")

fun addedToFavouritesMessage(eventsCount: Int): String =
    """$eventsCount
            |${if (eventsCount > 1) " events were" else " event was"} 
            |added to favourites.""".trimMargin().replace("\n", "")

fun removedFromFavouritesMessage(eventsCount: Int): String =
    """$eventsCount
            |${if (eventsCount > 1) " events were" else " event was"} 
            |removed from favourites.""".trimMargin().replace("\n", "")
