package com.example.favourites

import com.example.core.model.event.IEvent
import com.example.core.util.DataList
import com.example.core.util.LoadedSuccessfully
import com.example.coreandroid.base.ClearSelectionUpdate
import com.example.coreandroid.base.EventSelectionConfirmedUpdate
import com.example.coreandroid.base.StateUpdate
import com.example.coreandroid.base.ToggleEventSelectionUpdate
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable

sealed class FavouritesStateUpdate : StateUpdate<FavouritesState> {
    data class SearchText(val searchText: String) : FavouritesStateUpdate() {
        override fun invoke(state: FavouritesState): FavouritesState = state.copy(
            searchText = searchText
        )
    }

    data class ToggleEventSelection(
        override val event: Event
    ) : FavouritesStateUpdate(),
        ToggleEventSelectionUpdate<FavouritesState>

    object ClearSelection :
        FavouritesStateUpdate(),
        ClearSelectionUpdate<FavouritesState>

    object HideSnackbar : FavouritesStateUpdate() {
        override fun invoke(state: FavouritesState): FavouritesState = state
            .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
    }

    data class Events(val events: List<IEvent>) : FavouritesStateUpdate() {
        override fun invoke(state: FavouritesState): FavouritesState = state.copy(
            events = DataList(
                data = events.map { Selectable(Event(it)) },
                status = LoadedSuccessfully,
                limitHit = state.events.data.size == events.size
            ),
            limit = events.size
        )
    }

    data class RemovedFromFavourites(
        override val snackbarText: String,
        override val onSnackbarDismissed: () -> Unit
    ) : FavouritesStateUpdate(),
        EventSelectionConfirmedUpdate<FavouritesState>
}
