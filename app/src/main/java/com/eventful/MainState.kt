package com.eventful

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.model.location.LocationState

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
