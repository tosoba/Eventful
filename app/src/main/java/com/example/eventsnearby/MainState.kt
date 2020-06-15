package com.example.eventsnearby

import androidx.lifecycle.SavedStateHandle
import com.example.coreandroid.model.location.LocationState

data class MainState(
    val connected: Boolean = false,
    val locationState: LocationState = LocationState()
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        locationState = savedStateHandle[KEY_LOCATION_STATE] ?: LocationState()
    )

    companion object {
        const val KEY_LOCATION_STATE = "key_location_state"
    }
}
