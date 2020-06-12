package com.example.nearby

import com.example.core.util.PagedDataList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

internal sealed class NearbyViewUpdate {
    data class Events(val events: PagedDataList<Selectable<Event>>) : NearbyViewUpdate()
    data class Snackbar(val state: SnackbarState) : NearbyViewUpdate()
    data class UpdateActionMode(val numberOfSelectedEvents: Int) : NearbyViewUpdate()
    object FinishActionMode : NearbyViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
internal val NearbyViewModel.viewUpdates: Flow<NearbyViewUpdate>
    get() = merge(
        states.map { it.events }
            .distinctUntilChanged()
            .map { NearbyViewUpdate.Events(it) },
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { NearbyViewUpdate.Snackbar(it) },
        states.map { state -> state.events.data.count { it.selected } }
            .distinctUntilChanged()
            .map { NearbyViewUpdate.UpdateActionMode(it) },
        signals.filterIsInstance<NearbySignal.FavouritesSaved>()
            .map { NearbyViewUpdate.FinishActionMode }
    )
