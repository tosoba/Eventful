package com.example.coreandroid.base

import com.example.core.model.app.LocationState
import kotlinx.coroutines.flow.Flow

interface LocationStateProvider {
    val locationStateFlow: Flow<LocationState>
    val locationState: LocationState
}