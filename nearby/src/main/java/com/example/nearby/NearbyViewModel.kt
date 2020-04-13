package com.example.nearby

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.app.LatLng
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.core.util.replace
import com.example.coreandroid.arch.BaseViewModel
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.SnackbarState
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*

sealed class NearbyIntent
object EventListScrolledToEnd : NearbyIntent()
data class EventLongClicked(val event: Event) : NearbyIntent()
object ClearSelectionClicked : NearbyIntent()
object AddToFavouritesClicked : NearbyIntent()

private fun NearbyState.reduce(resource: Resource<PagedResult<IEvent>>): NearbyState {
    return when (resource) {
        is Resource.Success -> copy(
            events = events.copyWithNewItems(
                //TODO: make distinctBy work on (Paged)DataList (to prevent duplicates between pages)
                resource.data.items.map { Selectable(Event(it)) }.distinctBy { it.item.name },
                resource.data.currentPage + 1,
                resource.data.totalPages
            )
        )

        is Resource.Error<PagedResult<IEvent>, *> -> copy(
            events = events.copyWithError(resource.error),
            snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
                if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504)
                    SnackbarState.Text("No connection")
                else SnackbarState.Text("Unknown network error")
            } else snackbarState
        )
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
class NearbyVM(
    private val getNearbyEvents: GetNearbyEvents,
    private val saveEvents: SaveEvents,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val locationStateProvider: LocationStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: NearbyState = NearbyState.INITIAL
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
        filterIsInstance<EventLongClicked>().processEventClickedIntents(),
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
        emit(state.copy(events = state.events.copyWithLoadingInProgress))
        val result = withContext(ioDispatcher) {
            getNearbyEvents(latLng.lat, latLng.lng, state.events.offset)
        }
        state.reduce(result)
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

    private fun Flow<EventLongClicked>.processEventClickedIntents(): Flow<NearbyState> {
        return map { (event) ->
            val state = statesChannel.value
            state.copy(
                events = state.events.copy(
                    value = state.events.value.replace(
                        { matched -> Selectable(event, !matched.selected) },
                        { it.item.id == event.id }
                    )
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

class NearbyViewModel(
    private val getNearbyEvents: GetNearbyEvents,
    private val saveEvents: SaveEvents,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<NearbyState>(NearbyState.INITIAL) {

    private val signalsChannel: BroadcastChannel<NearbySignal> = ConflatedBroadcastChannel()
    val signalsFlow: Flow<NearbySignal> get() = signalsChannel.asFlow()

    fun loadEvents(userLatLng: LatLng) = withState { state ->
        if (state.events.status is Loading || state.events.offset >= state.events.totalItems)
            return@withState

        viewModelScope.launch {
            setState { copy(events = events.copyWithLoadingInProgress) }
            when (val result = withContext(ioDispatcher) {
                getNearbyEvents(userLatLng.lat, userLatLng.lng, state.events.offset)
            }) {
                is Resource.Success -> setState {
                    copy(
                        events = events.copyWithNewItems(
                            //TODO: make distinctBy work on (Paged)DataList (to prevent duplicates between pages)
                            result.data.items.map { Selectable(Event(it)) }
                                .distinctBy { it.item.name },
                            result.data.currentPage + 1,
                            result.data.totalPages
                        )
                    )
                }

                is Resource.Error<PagedResult<IEvent>, *> -> setState {
                    copy(
                        events = events.copyWithError(result.error),
                        snackbarState = if (result.error is NetworkResponse.ServerError<*>) {
                            if ((result.error as NetworkResponse.ServerError<*>).code in 503..504)
                                SnackbarState.Text("No connection")
                            else SnackbarState.Text("Unknown network error")
                        } else snackbarState
                    )
                }
            }
        }
    }

    fun addEventsToFavourites() = withState { state ->
        viewModelScope.launch {
            withContext(ioDispatcher) {
                saveEvents(state.events.value.filter { it.selected }.map { it.item })
                clearSelection()
                signalsChannel.send(NearbySignal.FavouritesSaved)
            }
        }
    }

    fun clearSelection() = setState {
        copy(events = events.copy(value = events.value.map { it.copy(selected = false) }))
    }

    fun toggleEventSelection(event: Event) = setState {
        copy(events = events.copy(value = events.value.replace(
            { matched -> Selectable(event, !matched.selected) },
            { it.item.id == event.id }
        )))
    }

    fun onNotConnected() = setState {
        copy(
            events = events.copyWithError(NearbyError.NotConnected),
            snackbarState = SnackbarState.Text("No connection")
        )
    }

    fun onLocationNotLoadedYet() = setState {
        copy(
            events = events.copyWithError(NearbyError.LocationNotLoadedYet),
            snackbarState = SnackbarState.Text("Retrieving location...")
        )
    }

    fun onLocationUnavailable() = setState {
        copy(
            events = events.copyWithError(NearbyError.LocationUnavailable),
            snackbarState = SnackbarState.Text("Location unavailable")
        )
    }
}