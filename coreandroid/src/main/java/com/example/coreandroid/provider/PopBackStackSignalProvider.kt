package com.example.coreandroid.provider

import kotlinx.coroutines.flow.Flow

interface PopBackStackSignal

interface PopBackStackSignalProvider {
    val popBackStackSignals: Flow<PopBackStackSignal>
}
