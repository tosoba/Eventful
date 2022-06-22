package com.eventful.event.details

import com.eventful.core.android.model.event.Event
import com.eventful.core.util.LoadedSuccessfully
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

internal sealed class EventDetailsViewUpdate {
    data class FloatingActionButtonDrawable(val isFavourite: Boolean) : EventDetailsViewUpdate()
    data class FavouriteStatusSnackbar(val isFavourite: Boolean) : EventDetailsViewUpdate()
    data class NewEvent(val event: Event) : EventDetailsViewUpdate()
}

@FlowPreview
@ExperimentalCoroutinesApi
internal val EventDetailsViewModel.viewUpdates: Flow<EventDetailsViewUpdate>
    get() =
        merge(
            states
                .map { it.isFavourite }
                .filter { (_, status) -> status is LoadedSuccessfully }
                .map { it.data }
                .filterNotNull()
                .distinctUntilChanged()
                .map { EventDetailsViewUpdate.FloatingActionButtonDrawable(it) },
            states.map { EventDetailsViewUpdate.NewEvent(it.event) },
            signals.filterIsInstance<EventDetailsSignal.FavouriteStateToggled>().map { (isFavourite)
                ->
                EventDetailsViewUpdate.FavouriteStatusSnackbar(isFavourite)
            })
