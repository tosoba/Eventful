package com.eventful

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowProcessor
import com.eventful.core.android.model.location.LocationStatus
import com.eventful.core.model.location.LocationResult
import com.eventful.core.usecase.alarm.GetUpcomingAlarms
import com.eventful.core.usecase.connection.IsConnectedFlow
import com.eventful.core.usecase.event.GetUpcomingEventsFlow
import com.eventful.core.usecase.location.GetLocation
import com.eventful.core.usecase.location.IsLocationAvailableFlow
import com.eventful.core.util.ext.flatMapFirst
import com.eventful.core.util.ext.takeWhileInclusive
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class MainFlowProcessor
@Inject
constructor(
    private val getLocation: GetLocation,
    private val isConnectedFlow: IsConnectedFlow,
    private val isLocationAvailableFlow: IsLocationAvailableFlow,
    private val getUpcomingAlarms: GetUpcomingAlarms,
    private val getUpcomingEvents: GetUpcomingEventsFlow
) : FlowProcessor<MainIntent, MainStateUpdate, MainState, Unit> {
    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<MainIntent>,
        currentState: () -> MainState,
        states: Flow<MainState>,
        intent: suspend (MainIntent) -> Unit,
        signal: suspend (Unit) -> Unit
    ): Flow<MainStateUpdate> =
        merge(
            intents.updates(currentState),
            isConnectedFlow().distinctUntilChanged().map { MainStateUpdate.Connection(it) },
            locationAvailabilityUpdates(states),
            getUpcomingAlarms(UPCOMING_ITEMS_LIMIT).distinctUntilChanged().map { alarms ->
                MainStateUpdate.UpcomingAlarms(alarms)
            },
            getUpcomingEvents(UPCOMING_ITEMS_LIMIT).distinctUntilChanged().map { events ->
                MainStateUpdate.UpcomingEvents(events)
            })

    override fun stateWillUpdate(
        currentState: MainState,
        nextState: MainState,
        update: MainStateUpdate,
        savedStateHandle: SavedStateHandle
    ) {
        if (update is MainStateUpdate.Location) {
            savedStateHandle[MainState.KEY_LOCATION_STATE] = nextState.locationState
        }
    }

    private fun Flow<MainIntent>.updates(currentState: () -> MainState): Flow<MainStateUpdate> =
        merge(
            filterIsInstance<MainIntent.LoadLocation>().take(1).flatMapLatest {
                locationLoadingUpdates
            },
            filterIsInstance<MainIntent.ReloadLocation>().reloadLocationUpdates(currentState),
            filterIsInstance<MainIntent.PermissionDenied>().map {
                MainStateUpdate.Location.PermissionDenied
            })

    private fun Flow<MainIntent.ReloadLocation>.reloadLocationUpdates(
        currentState: () -> MainState
    ): Flow<MainStateUpdate> =
        filterNot { currentState().locationState.status is LocationStatus.Loading }
            .flatMapFirst {
                locationLoadingUpdates.onStart { emit(MainStateUpdate.Location.Reset) }
            }

    private fun locationAvailabilityUpdates(states: Flow<MainState>): Flow<MainStateUpdate> =
        states
            .map { it.locationState }
            .filter { (latLng, status) -> status is LocationStatus.Disabled && latLng == null }
            .flatMapLatest { isLocationAvailableFlow().takeWhileInclusive { !it } }
            .filter { it }
            .flatMapLatest { locationLoadingUpdates }

    private val locationLoadingUpdates: Flow<MainStateUpdate>
        get() =
            flow<MainStateUpdate> {
                emit(MainStateUpdate.Location.Result(LocationResult.Loading))
                emit(MainStateUpdate.Location.Result(getLocation()))
            }

    companion object {
        private const val UPCOMING_ITEMS_LIMIT = 3
    }
}
