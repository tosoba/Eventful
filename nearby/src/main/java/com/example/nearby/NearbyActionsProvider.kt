package com.example.nearby

import com.example.core.IEventsRepository
import com.example.core.`do`
import com.example.coreandroid.arch.state.BaseFeature
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NearbyActionsProvider(
    private val repo: IEventsRepository
) : BaseFeature() {

    @ExperimentalCoroutinesApi
    fun CoroutineScope.getEvents(latLng: LatLng, offset: Int?) = produceActions<NearbyState> {
        stateTransition { copy(events = events.withLoadingInProgress) }
        repo.getNearbyEvents(
            lat = latLng.latitude, lon = latLng.longitude, offset = offset
        ).`do`(
            onSuccess = { (newEvents, newOffset) ->
                stateTransition { copy(events = events.copyWithNewItems(newEvents, newOffset)) }
            },
            onError = { stateTransition { copy(events = events.copyWithError(it)) } }
        )
    }
}