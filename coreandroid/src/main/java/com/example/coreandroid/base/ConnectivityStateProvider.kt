package com.example.coreandroid.base

import androidx.lifecycle.LiveData

interface ConnectivityStateProvider {
    val isConnectedLive: LiveData<Boolean>
    val isConnected: Boolean
}