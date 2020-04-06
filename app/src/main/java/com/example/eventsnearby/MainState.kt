package com.example.eventsnearby

import com.example.core.model.app.LocationState
import com.haroldadmin.vector.VectorState

data class MainState(
    val isConnected: Boolean,
    val locationState: LocationState
) : VectorState {
    val locationDisabledOrUnknown: Boolean
        get() = locationState is LocationState.Disabled || locationState is LocationState.Unknown

    companion object {
        val INITIAL: MainState
            get() = MainState(
                isConnected = false,
                locationState = LocationState.Unknown
            )
    }
}