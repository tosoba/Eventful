package com.example.core.provider

import com.example.core.model.app.LocationState
import kotlinx.coroutines.flow.Flow

interface LocationStateProvider {
    val locationStates: Flow<LocationState>
    fun reloadLocation()
}
