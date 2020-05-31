package com.example.eventsnearby

import com.example.core.model.app.LocationState

data class MainState(
    val connected: Boolean = false,
    val locationState: LocationState = LocationState()
)
