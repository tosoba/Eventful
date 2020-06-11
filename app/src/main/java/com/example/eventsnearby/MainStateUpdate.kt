package com.example.eventsnearby

import com.example.core.model.app.LocationResult
import com.example.core.model.app.LocationStatus
import com.example.coreandroid.util.StateUpdate

sealed class MainStateUpdate :
    StateUpdate<MainState> {
    class Connection(private val connected: Boolean) : MainStateUpdate() {
        override fun invoke(state: MainState): MainState = state.copy(connected = connected)
    }

    sealed class Location : MainStateUpdate() {
        object PermissionDenied : MainStateUpdate() {
            override fun invoke(state: MainState): MainState = state.copy(
                locationState = state.locationState.copy(status = LocationStatus.PermissionDenied)
            )
        }

        object Reset : MainStateUpdate() {
            override fun invoke(state: MainState): MainState = state.copy(
                locationState = state.locationState.copy(status = LocationStatus.Initial)
            )
        }

        class Result(private val result: LocationResult) : Location() {
            override fun invoke(state: MainState): MainState = state.copy(
                locationState = if (result is LocationResult.Found) state.locationState.copy(
                    latLng = result.latLng,
                    status = LocationStatus.Found
                ) else state.locationState.copy(
                    status = when (result) {
                        is LocationResult.Loading -> LocationStatus.Loading
                        is LocationResult.Disabled -> LocationStatus.Disabled
                        is LocationResult.Error -> LocationStatus.Error(
                            result.throwable
                        )
                        else -> throw IllegalArgumentException()
                    }
                )
            )
        }
    }
}
