package com.example.coreandroid.provider

import com.example.core.model.app.LocationState
import kotlinx.coroutines.flow.Flow

interface LocationStateProvider {
    val locationStateFlow: Flow<LocationState>
    fun reloadLocation()
}