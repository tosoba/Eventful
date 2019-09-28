package com.example.nearby

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetEvents
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.Loading
import com.google.android.gms.maps.model.LatLng
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearbyViewModel(
    private val getEvents: GetEvents,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<NearbyState>(NearbyState.INITIAL) {

    fun loadEvents(userLatLng: LatLng) = viewModelScope.launch {
        withState { state ->
            if (state.events.status is Loading || state.events.offset >= state.events.totalItems)
                return@withState

            setState { copy(events = events.copyWithLoadingInProgress) }
            when (val result = withContext(ioDispatcher) {
                getEvents(userLatLng.latitude, userLatLng.longitude, state.events.offset)
            }) {
                is Resource.Success -> setState {
                    copy(
                        events = events.copyWithNewItems(
                            result.data.items.map { Event(it) },
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