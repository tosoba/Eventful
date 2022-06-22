package com.eventful

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.model.location.LocationState

data class MainState(
    val connected: Boolean = false,
    val locationState: LocationState = LocationState(),
    val upcomingAlarms: List<Alarm> = emptyList(),
    val upcomingEvents: List<Event> = emptyList()
) {
    constructor(
        savedStateHandle: SavedStateHandle
    ) : this(locationState = savedStateHandle[KEY_LOCATION_STATE] ?: LocationState())

    companion object {
        const val KEY_LOCATION_STATE = "key_location_state"
    }
}
