package com.example.nearby

import com.example.core.Failure
import com.example.core.IEventsRepository
import com.example.core.Success
import com.example.core.model.Event
import com.example.coreandroid.arch.state.BaseFeature
import com.example.coreandroid.mapper.ui
import com.example.coreandroid.util.reverseGeocode
import com.google.android.gms.maps.model.LatLng
import com.patloew.rxlocation.RxLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NearbyActionsProvider(
    private val repo: IEventsRepository,
    private val rxLocation: RxLocation
) : BaseFeature() {

    @ExperimentalCoroutinesApi
    fun CoroutineScope.getEvents(latLng: LatLng, offset: Int?) = produceActions<NearbyState> {
        stateTransition { copy(events = events.withLoadingInProgress) }
        when (val result = repo.getNearbyEvents(
            lat = latLng.latitude, lon = latLng.longitude, offset = offset
        )) {
            is Success -> {
                val (newEvents, newOffset, totalItems) = result.data
                val uiEvents = newEvents.map(Event::ui)
                uiEvents.filter { it.androidLocation != null }.forEach { event ->
                    val address = rxLocation.reverseGeocode(event.androidLocation!!)
                    address?.let { event.address.set(it.thoroughfare) }
                }
                stateTransition {
                    copy(events = events.copyWithNewItems(uiEvents, newOffset, totalItems))
                }
            }
            is Failure -> {
                stateTransition { copy(events = events.copyWithError(result.error)) }
            }
        }
    }
}