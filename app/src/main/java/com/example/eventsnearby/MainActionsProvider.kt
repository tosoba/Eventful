package com.example.eventsnearby

import com.example.coreandroid.arch.state.BaseFeature
import com.example.coreandroid.util.LocationState
import com.example.coreandroid.util.awaitOne
import com.example.coreandroid.util.latLng
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActionsProvider(
    private val smartLocation: SmartLocation
) : BaseFeature() {

    @ExperimentalCoroutinesApi
    fun CoroutineScope.getLocation() = produceActions<MainState> {
        stateTransition { copy(locationState = LocationState.Loading) }
        try {
            smartLocation.location().run {
                if (state().locationServicesEnabled()) {
                    val location = awaitOne()
                    stateTransition { copy(locationState = LocationState.Found(location.latLng)) }
                } else {
                    stateTransition { copy(locationState = LocationState.Disabled) }
                }
            }
        } catch (e: Exception) {
            stateTransition { copy(locationState = LocationState.Error(e)) }
        }
    }
}