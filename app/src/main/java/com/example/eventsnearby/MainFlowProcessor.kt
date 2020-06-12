package com.example.eventsnearby

import androidx.lifecycle.SavedStateHandle
import com.example.core.model.app.LocationResult
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetLocation
import com.example.core.usecase.IsConnectedFlow
import com.example.core.usecase.IsLocationAvailableFlow
import com.example.core.util.ext.flatMapFirst
import com.example.core.util.ext.takeWhileInclusive
import com.example.coreandroid.base.FlowProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainFlowProcessor @Inject constructor(
    private val getLocation: GetLocation,
    private val isConnectedFlow: IsConnectedFlow,
    private val isLocationAvailableFlow: IsLocationAvailableFlow
) : FlowProcessor<MainIntent, MainStateUpdate, MainState, Unit> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<MainIntent>,
        currentState: () -> MainState,
        states: StateFlow<MainState>,
        intent: suspend (MainIntent) -> Unit,
        signal: suspend (Unit) -> Unit,
        savedStateHandle: SavedStateHandle
    ): Flow<MainStateUpdate> = merge(
        intents.updates(currentState),
        isConnectedFlow()
            .distinctUntilChanged()
            .map { MainStateUpdate.Connection(it) },
        locationAvailabilityUpdates(states)
    )

    private fun Flow<MainIntent>.updates(
        currentState: () -> MainState
    ): Flow<MainStateUpdate> = merge(
        filterIsInstance<MainIntent.LoadLocation>()
            .take(1)
            .flatMapLatest { locationLoadingUpdates },
        filterIsInstance<MainIntent.ReloadLocation>()
            .reloadLocationUpdates(currentState),
        filterIsInstance<MainIntent.PermissionDenied>()
            .map { MainStateUpdate.Location.PermissionDenied }
    )

    private fun Flow<MainIntent.ReloadLocation>.reloadLocationUpdates(
        currentState: () -> MainState
    ): Flow<MainStateUpdate> = filterNot {
        currentState().locationState.status is LocationStatus.Loading
    }.flatMapFirst {
        locationLoadingUpdates.onStart { emit(MainStateUpdate.Location.Reset) }
    }

    private fun locationAvailabilityUpdates(
        states: StateFlow<MainState>
    ): Flow<MainStateUpdate> = states.map { it.locationState }
        .filter { (latLng, status) -> status is LocationStatus.Disabled && latLng == null }
        .flatMapLatest { isLocationAvailableFlow().takeWhileInclusive { !it } }
        .filter { it }
        .flatMapLatest { locationLoadingUpdates }

    private val locationLoadingUpdates: Flow<MainStateUpdate>
        get() = flow<MainStateUpdate> {
            emit(MainStateUpdate.Location.Result(LocationResult.Loading))
            emit(MainStateUpdate.Location.Result(getLocation()))
        }
}