package com.example.eventsnearby

import androidx.lifecycle.LiveData
import com.example.coreandroid.arch.state.ViewStateStore
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.CoroutineViewModel
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.util.LocationState
import com.example.coreandroid.util.SnackbarState
import com.example.coreandroid.util.awaitOne
import com.example.coreandroid.util.latLng
import com.shopify.livedataktx.map
import com.shopify.livedataktx.nonNull
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainViewModel(
    private val smartLocation: SmartLocation
) : CoroutineViewModel<MainState>(ViewStateStore(MainState.INITIAL)),
    ConnectivityStateProvider, LocationStateProvider {

    override val isConnectedLive: LiveData<Boolean>
        get() = stateStore.liveState.nonNull().map { state: MainState -> state.isConnected }

    override val isConnected: Boolean
        get() = stateStore.currentState.isConnected

    override val locationStateLive: LiveData<LocationState>
        get() = stateStore.liveState.nonNull().map { state: MainState -> state.locationState }

    override val locationState: LocationState
        get() = stateStore.currentState.locationState

    fun storeSnackbarState(state: SnackbarState) {
        stateStore.transition { copy(snackbarState = state) }
    }

    fun loadLocation() {
        launch {
            stateStore.transition { copy(locationState = LocationState.Loading) }
            try {
                smartLocation.location().run {
                    if (state().locationServicesEnabled()) {
                        val location = awaitOne()
                        stateStore.transition { copy(locationState = LocationState.Found(location.latLng)) }
                    } else {
                        stateStore.transition { copy(locationState = LocationState.Disabled) }
                    }
                }
            } catch (e: Exception) {
                stateStore.transition { copy(locationState = LocationState.Error(e)) }
            }
        }
    }

    fun onConnectionStateChanged(isConnected: Boolean) {
        stateStore.transition { copy(isConnected = isConnected) }
    }

    fun onPermissionDenied() {
        stateStore.transition { copy(locationState = LocationState.PermissionDenied) }
    }
}