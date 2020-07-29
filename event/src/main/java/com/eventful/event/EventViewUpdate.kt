package com.eventful.event

import com.eventful.core.util.LoadedSuccessfully
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

internal sealed class EventViewUpdate {
    data class FloatingActionButtonDrawable(val isFavourite: Boolean) : EventViewUpdate()
    data class FavouriteStatusSnackbar(val isFavourite: Boolean) : EventViewUpdate()
}

@FlowPreview
@ExperimentalCoroutinesApi
internal val EventViewModel.viewUpdates: Flow<EventViewUpdate>
    get() = merge(
        states.map { it.isFavourite }
            .filter { (_, status) -> status is LoadedSuccessfully }
            .map { it.data }
            .filterNotNull()
            .distinctUntilChanged()
            .map { EventViewUpdate.FloatingActionButtonDrawable(it) },
        signals.filterIsInstance<EventSignal.FavouriteStateToggled>()
            .map { (isFavourite) -> EventViewUpdate.FavouriteStatusSnackbar(isFavourite) }
    )
