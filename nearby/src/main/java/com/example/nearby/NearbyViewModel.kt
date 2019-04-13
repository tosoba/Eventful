package com.example.nearby

import androidx.lifecycle.ViewModel
import com.example.coreandroid.arch.state.CoViewStateStore
import com.example.coreandroid.arch.state.StateObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class NearbyViewModel(
    private val actionsProvider: NearbyActionsProvider
) : ViewModel() {

    private val viewStateStore = CoViewStateStore(NearbyState.INITIAL, Dispatchers.IO)
    val viewStateObservable: StateObservable<NearbyState> = viewStateStore

    fun loadEvents() {
        viewStateStore.coDispatch { state ->
            actionsProvider.run {
                getEvents(latLng = state.userLatLng, offset = state.events.offset)
            }
        }
    }

    fun onNotConnected() {
        viewStateStore.dispatchStateTransition {
            copy(events = events.copyWithError(NearbyError.NotConnected))
        }
    }

    fun onLocationUnavailable() {
        viewStateStore.dispatchStateTransition {
            copy(events = events.copyWithError(NearbyError.LocationUnavailable))
        }
    }

    override fun onCleared() = viewStateStore.dispose()
}