package com.example.nearby

import android.view.View
import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LatLng
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.controller.SnackbarAction
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.processClearSelectionIntents
import com.example.coreandroid.util.processEventLongClickedIntents
import com.example.coreandroid.util.withLatestFrom
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
        }.withLatestFrom(locationStateProvider.locationStateFlow.notNullLatLng) { _, latLng ->
            latLng
        }.flatMapConcat { loadingEventsFlow(it) }

    private val locationSnackbarFlow: Flow<NearbyState> //TODO: zip this with connection status?
        get() = locationStateProvider.locationStateFlow
            .filter { it.latLng == null }
            .map { (_, status) ->
                state.run {
                    when (status) {
                        LocationStatus.PermissionDenied -> copy(
                            snackbarState = SnackbarState.Text("No location permission")
                        )
                        LocationStatus.Disabled -> copy(
                            snackbarState = SnackbarState.Text("Location disabled")
                        )
                        LocationStatus.Loading -> copy(
                            snackbarState = SnackbarState.Text("Loading location...")
                        )
                        is LocationStatus.Error -> copy(
                            snackbarState = SnackbarState.Text(
                                "Unable to load location - error occurred",
                                action = SnackbarAction(
                                    "Retry",
                                    View.OnClickListener { locationStateProvider.reloadLocation() }
                                )
                            )
                        )
                        else -> null
                    }
                }
            }
            .filterNotNull()

    private val Flow<LocationState>.notNullLatLng get() = map { it.latLng }.filterNotNull()

    private val loadEventsFlow: Flow<NearbyState>
        get() = locationStateProvider.locationStateFlow
            .filter { state.events.data.isEmpty() }//TODO: this won't work with refreshing with SwipeRefreshLayout
            .notNullLatLng
            .flatMapConcat { loadingEventsFlow(it) }

    private fun loadingEventsFlow(latLng: LatLng): Flow<NearbyState> = flow {
        state.run {
            emit(
                copy(
                    events = events.copyWithLoadingStatus,
                    snackbarState = SnackbarState.Text("Loading nearby events...")
                )
            )
            val result = withContext(ioDispatcher) {
                getNearbyEvents(latLng.lat, latLng.lng, events.offset)
            }
            emit(reduce(result))
        }
    }

    private fun Flow<EventListScrolledToEnd>.processScrolledToEndIntents(): Flow<NearbyState> {
        return filterNot {
            statesChannel.value.run {
                events.status is Loading || events.data.isEmpty() || events.offset >= events.limit
            }
        }.withLatestFrom(locationStateProvider.locationStateFlow.notNullLatLng) { _, latLng ->
            latLng
        }.flatMapFirst { loadingEventsFlow(it) }
    }

    private fun Flow<AddToFavouritesClicked>.processAddToFavouritesIntents(): Flow<NearbyState> {
        return map {
            state.run {
                withContext(ioDispatcher) {
                    saveEvents(events.data.filter { it.selected }.map { it.item })
                }
                liveSignals.value = NearbySignal.FavouritesSaved
                copy(events = events.transformItems { it.copy(selected = false) }) //TODO: snackbar state with info how many added
            }
        }
    }
}
