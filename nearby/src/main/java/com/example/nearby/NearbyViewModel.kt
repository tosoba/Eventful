package com.example.nearby

import com.example.core.Failure
import com.example.core.IEventsRepository
import com.example.core.Success
import com.example.core.model.event.Event
import com.example.coreandroid.arch.state.ViewStateStore
import com.example.coreandroid.base.CoroutineViewModel
import com.example.coreandroid.mapper.ui
import com.example.coreandroid.util.reverseGeocode
import com.google.android.gms.maps.model.LatLng
import com.patloew.rxlocation.RxLocation
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class NearbyViewModel(
    private val repo: IEventsRepository,
    private val rxLocation: RxLocation,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineViewModel<NearbyState>(ViewStateStore(NearbyState.INITIAL)) {

    fun loadEvents(userLatLng: LatLng) {
        launch {
            viewStateStore.dispatchStateTransition { copy(events = events.withLoadingInProgress) }
            when (val result = withContext(ioDispatcher) {
                repo.getNearbyEvents(
                    lat = userLatLng.latitude,
                    lon = userLatLng.longitude,
                    offset = viewStateStore.currentState.events.offset
                )
            }) {
                is Success -> {
                    val (newEvents, newOffset, totalItems) = result.data
                    val uiEvents = newEvents.map(Event::ui)
                    withContext(ioDispatcher) {
                        uiEvents.filter { it.androidLocation != null }.forEach { event ->
                            val address = rxLocation.reverseGeocode(event.androidLocation!!)
                            address?.let {
                                if (it.thoroughfare != null) {
                                    val addressText =
                                        "${it.thoroughfare} ${if (it.subThoroughfare != null) it.subThoroughfare else ""}"
                                    event.address.set(addressText)
                                }
                            }
                        }
                    }
                    viewStateStore.dispatchStateTransition {
                        copy(events = events.copyWithNewItems(uiEvents, newOffset, totalItems))
                    }
                }
                is Failure -> {
                    viewStateStore.dispatchStateTransition { copy(events = events.copyWithError(result.error)) }
                }
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
}