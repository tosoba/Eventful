package com.eventful.core.android.provider

import kotlinx.coroutines.flow.Flow

interface ConnectedStateProvider {
    val connectedStates: Flow<Boolean>
}
