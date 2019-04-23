package com.example.coreandroid.base

import androidx.lifecycle.LiveData
import com.example.coreandroid.util.LocationState

interface LocationStateProvider {
    val locationStateLive: LiveData<LocationState>
    val locationState: LocationState
}