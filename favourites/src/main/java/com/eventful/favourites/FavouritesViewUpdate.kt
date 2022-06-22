package com.eventful.favourites

import com.eventful.core.android.controller.SnackbarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class FavouritesViewUpdate {
    data class Events(val eventsData: FavouriteEventsData) : FavouritesViewUpdate()
    data class Snackbar(val state: SnackbarState) : FavouritesViewUpdate()
    data class UpdateActionMode(val numberOfSelectedEvents: Int) : FavouritesViewUpdate()
    object FinishActionMode : FavouritesViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val FavouritesViewModel.viewUpdates: Flow<FavouritesViewUpdate>
    get() =
        merge(
            states
                .map { FavouriteEventsData(it.searchText, it.items) }
                .distinctUntilChanged()
                .map { FavouritesViewUpdate.Events(it) },
            states
                .map { it.snackbarState }
                .distinctUntilChanged()
                .map { FavouritesViewUpdate.Snackbar(it) },
            states
                .map { state -> state.items.data.count { it.selected } }
                .distinctUntilChanged()
                .map { FavouritesViewUpdate.UpdateActionMode(it) },
            signals.filterIsInstance<FavouritesSignal.FavouritesRemoved>().map {
                FavouritesViewUpdate.FinishActionMode
            })
