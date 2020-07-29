package com.eventful.core.android.provider

import com.eventful.core.android.model.location.LocationState
import kotlinx.coroutines.flow.Flow

interface LocationStateProvider {
    val locationStates: Flow<LocationState>
    fun reloadLocation()
}
