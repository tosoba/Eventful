package com.example.nearby

import android.view.View
import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.app.LatLng
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.controller.SnackbarAction
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.coreandroid.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


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
        filterIsInstance<ClearSelectionClicked>().withLatestState().processClearSelectionIntents(),
        filterIsInstance<EventLongClicked>().withLatestState().processEventLongClickedIntents(),
        filterIsInstance<HideSnackbarIntent>().withLatestState().processHideSnackbarIntents(),
        filterIsInstance<AddToFavouritesClicked>().withLatestState()
            .processAddToFavouritesIntentsWithSnackbar(
                saveEvents = saveEvents,
                ioDispatcher = ioDispatcher,
                onDismissed = { viewModelScope.launch { send(HideSnackbar) } },
                sideEffect = { liveSignals.value = NearbySignal.FavouritesSaved }
            ),
        filterIsInstance<EventListScrolledToEnd>().withLatestState().processScrolledToEndIntents()
    )

    private val connectivityReactionFlow: Flow<NearbyState>
        get() = connectivityStateProvider.isConnectedFlow
            .withLatestState()
            .filter { (isConnected, currentState) ->
                isConnected && currentState.events.loadingFailed && currentState.events.data.isEmpty()
            }
            .withLatestFrom(locationStateProvider.locationStateFlow.notNullLatLng) { connectedWithState, latLng ->
                connectedWithState to latLng
            }
            .flatMapConcat { (connectedWithState, latLng) ->
                val (_, currentState) = connectedWithState
                loadingEventsFlow(latLng, currentState)
            }

    private val locationSnackbarFlow: Flow<NearbyState> //TODO: zip this with connection status?
        get() = locationStateProvider.locationStateFlow
            .filter { it.latLng == null }
            .withLatestState()
            .map { (location, currentState) ->
                val (_, status) = location
                when (status) {
                    LocationStatus.PermissionDenied -> currentState.copy(
                        snackbarState = SnackbarState.Shown("No location permission")
                    )
                    LocationStatus.Disabled -> currentState.copy(
                        snackbarState = SnackbarState.Shown("Location disabled")
                    )
                    LocationStatus.Loading -> currentState.copy(
                        snackbarState = SnackbarState.Shown("Loading location...")
                    )
                    is LocationStatus.Error -> currentState.copy(
                        snackbarState = SnackbarState.Shown(
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
            .filterNotNull()

    private val loadEventsFlow: Flow<NearbyState>
        get() = locationStateProvider.locationStateFlow
            .notNullLatLng
            .withLatestState()
            .filter { (_, currentState) -> currentState.events.data.isEmpty() } //TODO: this won't work with refreshing with SwipeRefreshLayout
            .flatMapConcat { (latLng, currentState) -> loadingEventsFlow(latLng, currentState) }

    private fun Flow<Pair<EventListScrolledToEnd, NearbyState>>.processScrolledToEndIntents(): Flow<NearbyState> {
        return filterNot { (_, currentState) ->
            val events = currentState.events
            events.status is Loading || !events.canLoadMore || events.data.isEmpty()
        }.withLatestFrom(locationStateProvider.locationStateFlow.notNullLatLng) { intentWithState, latLng ->
            val (_, currentState) = intentWithState
            latLng to currentState
        }.flatMapLatest { loadingEventsFlow(it.first, it.second) }
    }

    private val Flow<LocationState>.notNullLatLng get() = map { it.latLng }.filterNotNull()

    private fun loadingEventsFlow(
        latLng: LatLng, startState: NearbyState
    ): Flow<NearbyState> {
        val nextEventsResourceFlow = flow {
            var offset = startState.events.offset
            var resource: Resource<PagedResult<IEvent>>
            do {
                resource = withContext(ioDispatcher) {
                    getNearbyEvents(latLng.lat, latLng.lng, startState.events.offset)
                }
                val newState = startState.reduce(resource)
                ++offset
            } while (newState.events.status is LoadedSuccessfully
                && newState.events.data.size == startState.events.data.size
            )
            emit(resource)
        }
        return nextEventsResourceFlow.withLatestState()
            .map { (resource, currentState) -> currentState.reduce(resource) }
            .onStart { emit(startState.copy(events = startState.events.copyWithLoadingStatus)) }
    }
}
