package com.eventful.favourites

import com.eventful.core.model.event.IEvent
import com.eventful.core.util.DataList
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.android.base.ClearSelectionUpdate
import com.eventful.core.android.base.ItemSelectionConfirmedUpdate
import com.eventful.core.android.base.StateUpdate
import com.eventful.core.android.base.ToggleItemSelectionUpdate
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable

sealed class FavouritesStateUpdate : StateUpdate<FavouritesState> {
    data class SearchText(val searchText: String) : FavouritesStateUpdate() {
        override fun invoke(state: FavouritesState): FavouritesState = state.copy(
            searchText = searchText
        )
    }

    data class ToggleEventSelection(
        override val item: Event
    ) : FavouritesStateUpdate(),
        ToggleItemSelectionUpdate<FavouritesState, Event, String> {
        override fun Event.id(): String = id
    }

    object ClearSelection :
        FavouritesStateUpdate(),
        ClearSelectionUpdate<FavouritesState, Event>

    object HideSnackbar : FavouritesStateUpdate() {
        override fun invoke(state: FavouritesState): FavouritesState = state
            .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
    }

    data class Events(val events: List<IEvent>) : FavouritesStateUpdate() {
        override fun invoke(state: FavouritesState): FavouritesState = state.copy(
            items = DataList(
                data = events.map { Selectable(Event(it)) },
                status = LoadedSuccessfully,
                limitHit = state.items.data.size == events.size
            ),
            limit = events.size
        )
    }

    data class RemovedFromFavourites(
        override val snackbarText: String,
        override val onSnackbarDismissed: () -> Unit
    ) : FavouritesStateUpdate(),
        ItemSelectionConfirmedUpdate<FavouritesState, Event>
}
