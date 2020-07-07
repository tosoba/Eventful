package com.example.coreandroid.provider

import com.example.coreandroid.model.location.LocationState
import kotlinx.coroutines.flow.Flow

interface LocationStateProvider {
    val locationStates: Flow<LocationState>
    fun reloadLocation()
}
