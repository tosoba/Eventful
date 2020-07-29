package com.eventful.core.android.provider

import kotlinx.coroutines.flow.Flow

interface PopBackStackSignal

interface PopBackStackSignalProvider {
    val popBackStackSignals: Flow<PopBackStackSignal>
}
