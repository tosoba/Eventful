package com.example.eventsnearby

import com.example.coreandroid.util.LocationState
import com.example.coreandroid.util.SnackbarState
import com.haroldadmin.vector.VectorState

data class MainState(
    val isConnected: Boolean,
    val snackbarState: SnackbarState,
    val locationState: LocationState
) : VectorState {
    val locationDisabledOrUnknown: Boolean
        get() = locationState is LocationState.Disabled || locationState is LocationState.Unknown

    companion object {
        val INITIAL: MainState
            get() = MainState(false, SnackbarState.Hidden, LocationState.Unknown)
    }
}