package com.example.nearby

import android.util.Log
import com.example.core.Failure
import com.example.core.IEventsRepository
import com.example.core.Success
import com.example.core.model.event.Event
import com.example.coreandroid.arch.state.ViewStateStore
import com.example.coreandroid.base.CoroutineViewModel
import com.example.coreandroid.mapper.ui
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.util.PhotoSize
import com.example.coreandroid.util.callSuspending
import com.example.coreandroid.util.loadPhotosUrlsForLocation
import com.example.coreandroid.util.reverseGeocode
import com.flickr4java.flickr.Flickr
import com.google.android.gms.maps.model.LatLng
import com.patloew.rxlocation.RxLocation
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class NearbyViewModel(
    private val repo: IEventsRepository,
    private val rxLocation: RxLocation,
    private val flickr: Flickr,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineViewModel<NearbyState>(ViewStateStore(NearbyState.INITIAL)) {

    fun loadEvents(userLatLng: LatLng) = launch {
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
                awaitAll(
                    async { loadLocationsFor(uiEvents) },
                    async { loadPhotoUrlsFor(uiEvents) }
                )
                viewStateStore.dispatchStateTransition {
                    copy(events = events.copyWithNewItems(uiEvents, newOffset, totalItems))
                }
            }
            is Failure -> {
                viewStateStore.dispatchStateTransition { copy(events = events.copyWithError(result.error)) }
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

    private suspend fun loadLocationsFor(events: List<EventUiModel>) = withContext(ioDispatcher) {
        events.filter { it.androidLocation != null }
            .map {
                async { Pair(it, rxLocation.reverseGeocode(it.androidLocation!!)) }
            }
            .awaitAll()
            .forEach { (event, address) ->
                address?.let {
                    if (it.thoroughfare != null) {
                        val addressText =
                            "${it.thoroughfare} ${if (it.subThoroughfare != null) it.subThoroughfare else ""}"
                        event.address.set(addressText)
                    }
                }
            }
    }

    private suspend fun loadPhotoUrlsFor(events: List<EventUiModel>) = withContext(ioDispatcher) {
        events.filter { it.latLng != null }
            .map {
                async {
                    try {
                        Pair(it, flickr.callSuspending {
                            loadPhotosUrlsForLocation(it.latLng!!, it.category, 5, PhotoSize.MEDIUM_640)
                        })
                    } catch (e: Exception) {
                        Log.e("Flickr", e.message ?: "Unknown exception")
                        Pair(it, emptyList<String>())
                    }
                }
            }
            .awaitAll()
            .forEach { (event, photoUrls) ->
                if (photoUrls.isNotEmpty()) event.photoUrls.addAll(photoUrls)
            }
    }

}