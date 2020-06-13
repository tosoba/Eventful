package com.example.favourites

import com.example.core.util.DataList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

sealed class FavouritesViewUpdate {
    data class Events(val events: DataList<Selectable<Event>>) : FavouritesViewUpdate()
    data class Snackbar(val state: SnackbarState) : FavouritesViewUpdate()
    data class UpdateActionMode(val numberOfSelectedEvents: Int) : FavouritesViewUpdate()
    object FinishActionMode : FavouritesViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val FavouritesViewModel.viewUpdates: Flow<FavouritesViewUpdate>
    get() = kotlinx.coroutines.flow.merge(
        states.map { it.events }
            .distinctUntilChanged()
            .map { FavouritesViewUpdate.Events(it) },
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { FavouritesViewUpdate.Snackbar(it) },
        states.map { state -> state.events.data.count { it.selected } }
            .distinctUntilChanged()
            .map { FavouritesViewUpdate.UpdateActionMode(it) },
        signals.filterIsInstance<FavouritesSignal.FavouritesRemoved>()
            .map { FavouritesViewUpdate.FinishActionMode }
    )
