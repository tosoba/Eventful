package com.example.coreandroid.provider

import kotlinx.coroutines.flow.Flow

interface ConnectedStateProvider {
    val connectedStates: Flow<Boolean>
}