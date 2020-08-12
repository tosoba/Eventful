package com.eventful.core.android.base

import com.eventful.core.android.R
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.model.Selectable
import com.eventful.core.util.HoldsList
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
    val msgRes: SnackbarState.Shown.MsgRes
    val onSnackbarDismissed: () -> Unit

    override fun invoke(state: S): S = state.copyWithSnackbarStateAndTransformedItems(
        snackbarState = SnackbarState.Shown(
            msg = msgRes,
            length = Snackbar.LENGTH_SHORT,
            onDismissed = onSnackbarDismissed
        )
    ) { selectable ->
        selectable.copy(selected = false)
    }
}

fun removedAlarmsMsgRes(alarmsCount: Int): Int = if (alarmsCount > 1) {
    R.string.alarms_removed
} else {
    R.string.alarm_removed
}

fun addedToFavouritesMsgRes(eventsCount: Int): Int = if (eventsCount > 1) {
    R.string.events_added_to_favourites
} else {
    R.string.event_added_to_favourites
}

fun removedFromFavouritesMsgRes(eventsCount: Int): Int = if (eventsCount > 1) {
    R.string.events_removed_from_favourites
} else {
    R.string.event_removed_from_favourites
}
