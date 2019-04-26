package com.example.eventsnearby

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coreandroid.arch.state.CoViewStateStore
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.util.LocationState
import com.example.coreandroid.util.SnackbarState
import com.shopify.livedataktx.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainViewModel(
    private val actionsProvider: MainActionsProvider
) : ViewModel(), ConnectivityStateProvider, LocationStateProvider {
    val viewStateStore = CoViewStateStore(MainState.INITIAL, Dispatchers.IO)

    override val isConnectedLive: LiveData<Boolean>
        get() = viewStateStore.liveState.map { it!!.isConnected }

    override val isConnected: Boolean
        get() = viewStateStore.currentState.isConnected

    override val locationStateLive: LiveData<LocationState>
        get() = viewStateStore.liveState.map { it!!.locationState }

    override val locationState: LocationState
        get() = viewStateStore.currentState.locationState

    fun storeSnackbarState(state: SnackbarState) {
        viewStateStore.dispatchStateTransition { copy(snackbarState = state) }
    }

    fun loadLocation() {
        viewStateStore.coDispatch { _ ->
            actionsProvider.run {
                getLocation()
            }
        }
    }

    override fun onCleared() = viewStateStore.dispose()
}