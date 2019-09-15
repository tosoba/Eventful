package com.example.coreandroid.base

import kotlinx.coroutines.flow.Flow


interface ConnectivityStateProvider {
    val isConnectedFlow: Flow<Boolean>
    val isConnected: Boolean
}