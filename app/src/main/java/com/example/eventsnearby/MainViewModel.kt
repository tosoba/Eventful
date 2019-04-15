package com.example.eventsnearby

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coreandroid.arch.state.ViewDataStore
import com.example.coreandroid.base.ConnectivityStateProvider
import com.shopify.livedataktx.map

class MainViewModel : ViewModel(), ConnectivityStateProvider {
    val viewStateStore = ViewDataStore(MainState.INITIAL)
    override val isConnectedLive: LiveData<Boolean>
        get() = viewStateStore.liveState.map { it!!.isConnected }
    override val isConnected: Boolean
        get() = viewStateStore.currentState.isConnected
}