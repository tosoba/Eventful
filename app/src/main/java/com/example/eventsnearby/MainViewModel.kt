package com.example.eventsnearby

import androidx.lifecycle.viewModelScope
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.util.LocationState
import com.example.coreandroid.util.SnackbarState
import com.example.coreandroid.util.awaitOne
import com.example.coreandroid.util.latLng
import com.haroldadmin.vector.VectorViewModel
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainViewModel(
    private val smartLocation: SmartLocation
) : VectorViewModel<MainState>(MainState.INITIAL),
    ConnectivityStateProvider, LocationStateProvider {

    override val isConnectedFlow: Flow<Boolean>
        get() = state.map { it.isConnected }

    override val isConnected: Boolean get() = currentState.isConnected

    override val locationStateFlow: Flow<LocationState>
        get() = state.map { it.locationState }

    override val locationState: LocationState get() = currentState.locationState

    var connected: Boolean
        set(value) = setState { copy(isConnected = value) }
        get() = currentState.isConnected

    var snackbarState: SnackbarState
        set(value) = setState { copy(snackbarState = value) }
        get() = currentState.snackbarState

    fun loadLocation() = viewModelScope.launch {
        setState { copy(locationState = LocationState.Loading) }
        try {
            smartLocation.location().run {
                if (state().locationServicesEnabled()) {
                    val location = awaitOne()
                    setState { copy(locationState = LocationState.Found(location.latLng)) }
                } else {
                    setState { copy(locationState = LocationState.Disabled) }
                }
            }
        } catch (e: Exception) {
            setState { copy(locationState = LocationState.Error(e)) }
        }
    }

    fun onPermissionDenied() = setState { copy(locationState = LocationState.PermissionDenied) }
}