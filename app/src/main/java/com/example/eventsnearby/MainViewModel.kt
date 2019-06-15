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
        get() = viewStateStore.liveState.nonNull().map { it.isConnected }

    override val isConnected: Boolean
        get() = viewStateStore.currentState.isConnected

    override val locationStateLive: LiveData<LocationState>
        get() = viewStateStore.liveState.nonNull().map { it.locationState }

    override val locationState: LocationState
        get() = viewStateStore.currentState.locationState

    fun storeSnackbarState(state: SnackbarState) {
        viewStateStore.dispatchStateTransition { copy(snackbarState = state) }
    }

    fun loadLocation() {
        launch {
            viewStateStore.dispatchStateTransition { copy(locationState = LocationState.Loading) }
            try {
                smartLocation.location().run {
                    if (state().locationServicesEnabled()) {
                        val location = awaitOne()
                        viewStateStore.dispatchStateTransition { copy(locationState = LocationState.Found(location.latLng)) }
                    } else {
                        viewStateStore.dispatchStateTransition { copy(locationState = LocationState.Disabled) }
                    }
                }
            } catch (e: Exception) {
                viewStateStore.dispatchStateTransition { copy(locationState = LocationState.Error(e)) }
            }
        }
    }

    fun onConnectionStateChanged(isConnected: Boolean) {
        viewStateStore.dispatchStateTransition { copy(isConnected = isConnected) }
    }

    fun onPermissionDenied() {
        viewStateStore.dispatchStateTransition { copy(locationState = LocationState.PermissionDenied) }
    }
}