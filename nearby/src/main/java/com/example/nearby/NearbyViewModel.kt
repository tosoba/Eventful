package com.example.nearby

import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LatLng
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.coreandroid.arch.BaseViewModel
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.SnackbarState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext


@ExperimentalCoroutinesApi
@FlowPreview
class NearbyVM(
    private val getNearbyEvents: GetNearbyEvents,
    private val saveEvents: SaveEvents,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val locationStateProvider: LocationStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: NearbyState = NearbyState()
) : BaseViewModel<NearbyIntent, NearbyState, NearbySignal>(initialState) {

    init {
        merge(
            connectivityReactionFlow,
            locationSnackbarFlow,
            loadEventsFlow,
            intentsChannel.asFlow().processIntents()
        ).onEach(statesChannel::send).launchIn(viewModelScope)
    }

    private fun Flow<NearbyIntent>.processIntents(): Flow<NearbyState> = merge(
        filterIsInstance<ClearSelectionClicked>().processClearSelectionIntents(),
        filterIsInstance<EventLongClicked>().processEventLongClickedIntents(),
        filterIsInstance<AddToFavouritesClicked>().processAddToFavouritesIntents(),
        filterIsInstance<EventListScrolledToEnd>().processScrolledToEndIntents()
    )

    private val connectivityReactionFlow: Flow<NearbyState>
        get() = connectivityStateProvider.isConnectedFlow.filter { connected ->
            val state = statesChannel.value
            connected && state.events.value.isEmpty() && state.events.loadingFailed
        }.zip(locationStateProvider.locationStateFlow.notNullLatLng) { _, latLng ->
            latLng
        }.flatMapConcat { loadingEventsFlow(it) }

    private val locationSnackbarFlow: Flow<NearbyState>
        get() = locationStateProvider.locationStateFlow
            .filter { it.latLng == null }
            .map { (_, status) ->
                val state = statesChannel.value
                when (status) {
                    LocationStatus.PermissionDenied -> state.copy(
                        snackbarState = SnackbarState.Text("No location permission")
                    )
                    LocationStatus.Disabled -> state.copy(
                        snackbarState = SnackbarState.Text("Location disabled")
                    )
                    LocationStatus.Loading -> state.copy(
                        snackbarState = SnackbarState.Text("Loading location...")
                    )
                    is LocationStatus.Error -> state.copy(
                        snackbarState = SnackbarState.Text("Unable to load location - error occurred")
                    )
                    else -> null
                }
            }
            .filterNotNull()

    private val Flow<LocationState>.notNullLatLng get() = map { it.latLng }.filterNotNull()

    private val loadEventsFlow: Flow<NearbyState>
        get() = locationStateProvider.locationStateFlow
            .filter {
                val state = statesChannel.value
                state.events.value.isEmpty()
            }
            .notNullLatLng
            .flatMapConcat { loadingEventsFlow(it) }

    private fun loadingEventsFlow(latLng: LatLng): Flow<NearbyState> = flow {
        val state = statesChannel.value
        emit(
            state.copy(
                events = state.events.copyWithLoadingInProgress,
                snackbarState = SnackbarState.Text("Loading nearby events...")
            )
        )
        val result = withContext(ioDispatcher) {
            getNearbyEvents(latLng.lat, latLng.lng, state.events.offset)
        }
        emit(state.reduce(result))
    }

    private fun Flow<EventListScrolledToEnd>.processScrolledToEndIntents() = filterNot {
        val state = statesChannel.value
        state.events.status is Loading || state.events.offset >= state.events.totalItems
    }.zip(locationStateProvider.locationStateFlow.notNullLatLng) { _, latLng ->
        latLng
    }.flatMapConcat { loadingEventsFlow(it) }

    private fun Flow<ClearSelectionClicked>.processClearSelectionIntents(): Flow<NearbyState> {
        return map {
            val state = statesChannel.value
            state.copy(
                events = state.events.copy(
                    value = state.events.value.map { it.copy(selected = false) }
                )
            )
        }
    }

    private fun Flow<EventLongClicked>.processEventLongClickedIntents(): Flow<NearbyState> {
        return map { (event) ->
            val state = statesChannel.value
            state.copy(
                events = state.events.copy(
                    value = state.events.value.map {
                        if (it.item.id == event.id) Selectable(event, !it.selected) else it
                    }
                )
            )
        }
    }

    private fun Flow<AddToFavouritesClicked>.processAddToFavouritesIntents(): Flow<NearbyState> {
        return map {
            val state = statesChannel.value
            withContext(ioDispatcher) {
                saveEvents(state.events.value.filter { it.selected }.map { it.item })
            }
            liveEvents.value = NearbySignal.FavouritesSaved
            state.copy(
                events = state.events.copy(
                    value = state.events.value.map { it.copy(selected = false) }
                )
            )
        }
    }
}
