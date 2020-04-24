package com.example.nearby

import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LatLng
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.processClearSelectionIntents
import com.example.coreandroid.util.processEventLongClickedIntents
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext


@ExperimentalCoroutinesApi
@FlowPreview
class NearbyViewModel(
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
        filterIsInstance<ClearSelectionClicked>().processClearSelectionIntents { state },
        filterIsInstance<EventLongClicked>().processEventLongClickedIntents { state },
        filterIsInstance<AddToFavouritesClicked>().processAddToFavouritesIntents(),
        filterIsInstance<EventListScrolledToEnd>().processScrolledToEndIntents()
    )

    private val connectivityReactionFlow: Flow<NearbyState>
        get() = connectivityStateProvider.isConnectedFlow.filter { connected ->
            val state = statesChannel.value
            connected && state.events.data.isEmpty() && state.events.loadingFailed
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
                        snackbarState = SnackbarState.Text("Unable to load location - error occurred") //TODO: this appears before location is available...
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
                state.events.data.isEmpty() //TODO: this won't work with refreshing with SwipeRefreshLayout
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

    private fun Flow<AddToFavouritesClicked>.processAddToFavouritesIntents(): Flow<NearbyState> {
        return map {
            val state = statesChannel.value
            withContext(ioDispatcher) {
                saveEvents(state.events.data.filter { it.selected }.map { it.item })
            }
            liveEvents.value = NearbySignal.FavouritesSaved
            state.copy(
                events = state.events.transformItems { it.copy(selected = false) }
            )
        }
    }
}
