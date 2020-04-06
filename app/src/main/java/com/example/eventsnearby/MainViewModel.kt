package com.example.eventsnearby

import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationState
import com.example.core.usecase.GetLocation
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainViewModel(
    private val getLocation: GetLocation
) : VectorViewModel<MainState>(MainState.INITIAL),
    ConnectivityStateProvider,
    LocationStateProvider {

    override val isConnectedFlow: Flow<Boolean>
        get() = state.map { it.isConnected }

    override val isConnected: Boolean get() = currentState.isConnected

    override val locationStateFlow: Flow<LocationState>
        get() = state.map { it.locationState }

    override val locationState: LocationState get() = currentState.locationState

    var connected: Boolean
        set(value) = setState { copy(isConnected = value) }
        get() = currentState.isConnected

    fun loadLocation() = withState {
        if (it.locationState is LocationState.Loading) return@withState
        viewModelScope.launch {
            getLocation().collect { setState { copy(locationState = it) } }
        }
    }

    fun onPermissionDenied() = setState { copy(locationState = LocationState.PermissionDenied) }
}