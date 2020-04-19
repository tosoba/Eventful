package com.example.coreandroid.provider

import kotlinx.coroutines.flow.Flow

interface ConnectivityStateProvider {
    val isConnectedFlow: Flow<Boolean>
}