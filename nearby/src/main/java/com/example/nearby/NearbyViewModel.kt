package com.example.nearby

import androidx.lifecycle.viewModelScope
import com.example.core.IEventsRepository
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.coreandroid.arch.state.Loading
import com.example.coreandroid.ticketmaster.Event
import com.google.android.gms.maps.model.LatLng
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearbyViewModel(
    private val repo: IEventsRepository,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<NearbyState>(NearbyState.INITIAL) {

    fun loadEvents(userLatLng: LatLng) = viewModelScope.launch {
        withState { state ->
            if (state.events.status is Loading) {
                return@withState
            }

            setState { copy(events = events.copyWithLoadingInProgress) }
            when (val result = withContext(ioDispatcher) {
                repo.nearbyEvents(userLatLng.latitude, userLatLng.longitude, null)
            }) {
                is Resource.Success -> {
                    setState {
                        copy(
                            events = events.copyWithNewItems(
                                result.data.items.map { Event(it) },
                                result.data.currentPage + 1,
                                result.data.totalPages
                            )
                        )
                    }
                }

                is Resource.Error<PagedResult<IEvent>, *> -> {
                    setState { copy(events = events.copyWithError(result.error)) }
                }
            }
        }
    }

    fun onNotConnected() = setState {
        copy(events = events.copyWithError(NearbyError.NotConnected))
    }


    fun onLocationUnavailable() = setState {
        copy(events = events.copyWithError(NearbyError.LocationUnavailable))
    }
}