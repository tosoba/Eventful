package com.example.eventsnearby

import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.haroldadmin.vector.VectorState

data class MainState(
    val isConnected: Boolean,
    val locationState: LocationState
) : VectorState {
    companion object {
        val INITIAL: MainState
            get() = MainState(isConnected = false, locationState = LocationState())
    }
}