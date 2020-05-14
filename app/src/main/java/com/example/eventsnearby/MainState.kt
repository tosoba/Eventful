package com.example.eventsnearby

import com.example.core.model.app.LocationResult
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus

data class MainState(
    val isConnected: Boolean = false,
    val locationState: LocationState = LocationState()
)

internal fun MainState.reduce(result: LocationResult): MainState = copy(
    locationState = when (result) {
        is LocationResult.Found -> locationState.copy(
            latLng = result.latLng,
            status = LocationStatus.Found
        )
        is LocationResult.Loading -> locationState.copy(status = LocationStatus.Loading)
        else -> locationState.copy(
            status = when (result) {
                is LocationResult.Disabled -> LocationStatus.Disabled
                is LocationResult.Error -> LocationStatus.Error(
                    result.throwable
                )
                else -> locationState.status
            }
        )
    }
)