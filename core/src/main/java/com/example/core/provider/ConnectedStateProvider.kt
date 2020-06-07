package com.example.core.provider

import kotlinx.coroutines.flow.Flow

interface ConnectedStateProvider {
    val connectedStates: Flow<Boolean>
}