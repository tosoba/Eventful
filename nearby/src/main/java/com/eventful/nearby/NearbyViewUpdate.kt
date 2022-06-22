package com.eventful.nearby

import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable
import com.eventful.core.util.PagedDataList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class NearbyViewUpdate {
    data class Events(val events: PagedDataList<Selectable<Event>>) : NearbyViewUpdate()
    data class Snackbar(val state: SnackbarState) : NearbyViewUpdate()
    data class UpdateActionMode(val numberOfSelectedEvents: Int) : NearbyViewUpdate()
    object FinishActionMode : NearbyViewUpdate()
    object StopRefreshingIfInProgress : NearbyViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val NearbyViewModel.viewUpdates: Flow<NearbyViewUpdate>
    get() =
        merge(
            states.map { it.items }.distinctUntilChanged().map { NearbyViewUpdate.Events(it) },
            states
                .map { it.snackbarState }
                .distinctUntilChanged()
                .map { NearbyViewUpdate.Snackbar(it) },
            states
                .map { state -> state.items.data.count { it.selected } }
                .distinctUntilChanged()
                .map { NearbyViewUpdate.UpdateActionMode(it) },
            signals.filterIsInstance<NearbySignal.FavouritesSaved>().map {
                NearbyViewUpdate.FinishActionMode
            },
            signals.filterIsInstance<NearbySignal.EventsLoadingFinished>().map {
                NearbyViewUpdate.StopRefreshingIfInProgress
            })
