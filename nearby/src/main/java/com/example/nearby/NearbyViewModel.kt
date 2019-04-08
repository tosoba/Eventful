package com.example.nearby

import androidx.lifecycle.ViewModel
import com.example.coreandroid.arch.state.CoViewStateStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

class NearbyViewModel(
    private val actionsProvider: NearbyActionsProvider
) : ViewModel() {

    val viewStateStore = CoViewStateStore(NearbyState.INITIAL, Dispatchers.IO)

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    fun loadEvents() {
        viewStateStore.coDispatch { state ->
            actionsProvider.run { getEvents(latLng = state.userLatLng, offset = state.events.offset) }
        }
    }

    override fun onCleared() = viewStateStore.dispose()
}