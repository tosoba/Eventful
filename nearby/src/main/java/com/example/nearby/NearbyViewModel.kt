package com.example.nearby

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.app.LatLng
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetNearbyEvents
import com.example.core.util.replace
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.Loading
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearbyViewModel(
    private val getNearbyEvents: GetNearbyEvents,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<NearbyState>(NearbyState.INITIAL) {

    fun loadEvents(userLatLng: LatLng) = withState { state ->
        if (state.events.status is Loading || state.events.offset >= state.events.totalItems)
            return@withState

        viewModelScope.launch {
            setState { copy(events = events.copyWithLoadingInProgress) }
            when (val result = withContext(ioDispatcher) {
                getNearbyEvents(userLatLng.lat, userLatLng.lng, state.events.offset)
            }) {
                is Resource.Success -> setState {
                    copy(
                        events = events.copyWithNewItems(
                            //TODO: make distinctBy work on (Paged)DataList (to prevent duplicates between pages)
                            result.data.items.map { Selectable(Event(it)) }.distinctBy { it.item.name },
                            result.data.currentPage + 1,
                            result.data.totalPages
                        )
                    )
                }

                is Resource.Error<PagedResult<IEvent>, *> -> setState {
                    copy(events = events.copyWithError(result.error))
                }
            }
        }
    }

    fun toggleEventSelection(event: Event) = setState {
        copy(events = events.copy(value = events.value.replace(
            { matched -> Selectable(event, !matched.selected) },
            { it.item.id == event.id }
        )))
    }

    fun onNotConnected() = setState {
        copy(events = events.copyWithError(NearbyError.NotConnected))
    }

    fun onLocationNotLoadedYet() = setState {
        copy(events = events.copyWithError(NearbyError.LocationNotLoadedYet))
    }

    fun onLocationUnavailable() = setState {
        copy(events = events.copyWithError(NearbyError.LocationUnavailable))
    }
}