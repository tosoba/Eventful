package com.example.nearby

import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.usecase.SaveEvents
import com.example.core.util.Loading
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.FlowProcessor
import com.example.coreandroid.model.location.LocationState
import com.example.coreandroid.model.location.LocationStatus
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.coreandroid.base.addedToFavouritesMessage
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class NearbyFlowProcessor @Inject constructor(
    private val getNearbyEvents: GetNearbyEvents,
    private val saveEvents: SaveEvents,
    private val getPagedEventsFlow: GetPagedEventsFlow,
    private val connectedStateProvider: ConnectedStateProvider,
    private val locationStateProvider: LocationStateProvider,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<NearbyIntent, NearbyStateUpdate, NearbyState, NearbySignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<NearbyIntent>,
        currentState: () -> NearbyState,
        states: StateFlow<NearbyState>,
        intent: suspend (NearbyIntent) -> Unit,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = merge(
        intents.updates(coroutineScope, currentState, intent, signal),
        connectedStateProvider.updates(currentState),
        connectedStateProvider.snackbarUpdates(currentState),
        locationStateProvider.updates(currentState),
        locationStateProvider.snackbarUpdates
    )

    private fun Flow<NearbyIntent>.updates(
        coroutineScope: CoroutineScope,
        currentState: () -> NearbyState,
        intent: suspend (NearbyIntent) -> Unit,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = merge(
        filterIsInstance<NearbyIntent.ClearSelectionClicked>()
            .map { NearbyStateUpdate.ClearSelection },
        filterIsInstance<NearbyIntent.EventLongClicked>()
            .map { NearbyStateUpdate.ToggleEventSelection(it.event) },
        filterIsInstance<NearbyIntent.HideSnackbar>()
            .map { NearbyStateUpdate.HideSnackbar },
        filterIsInstance<NearbyIntent.LoadMoreResults>()
            .loadMoreResultsUpdates(currentState),
        filterIsInstance<NearbyIntent.AddToFavouritesClicked>()
            .addToFavouritesUpdates(coroutineScope, currentState, intent, signal)
    )

    private fun ConnectedStateProvider.updates(
        currentState: () -> NearbyState
    ): Flow<NearbyStateUpdate> = connectedStates.filter { connected ->
        currentState().run { connected && events.loadingFailed && events.data.isEmpty() }
    }.flatMapFirst {
        locationStateProvider.locationStates.notNullLatLng.take(1)
    }.flatMapLatest { latLng -> loadingEventsUpdates(latLng, currentState) }

    private fun ConnectedStateProvider.snackbarUpdates(
        currentState: () -> NearbyState
    ): Flow<NearbyStateUpdate> = connectedStates.filter { connected ->
        currentState().run { !connected && events.loadingFailed }
    }.flatMapFirst {
        locationStateProvider.locationStates.notNullLatLng.take(1)
    }.map { NearbyStateUpdate.NoConnectionSnackbar }

    private val LocationStateProvider.snackbarUpdates: Flow<NearbyStateUpdate>
        get() = locationStates.filter { it.latLng == null && it.status !is LocationStatus.Initial }
            .map { (_, status) ->
                NearbyStateUpdate.LocationSnackbar(status, locationStateProvider::reloadLocation)
            }

    private fun LocationStateProvider.updates(
        currentState: () -> NearbyState
    ): Flow<NearbyStateUpdate> = locationStates.notNullLatLng
        .filter { currentState().run { events.data.isEmpty() && !events.loadingFailed } } // TODO: this won't work with refreshing with SwipeRefreshLayout
        .flatMapLatest { latLng -> loadingEventsUpdates(latLng, currentState) }

    private fun Flow<NearbyIntent.LoadMoreResults>.loadMoreResultsUpdates(
        currentState: () -> NearbyState
    ): Flow<NearbyStateUpdate> = filterNot {
        val events = currentState().events
        events.status is Loading || !events.canLoadMore || events.data.isEmpty()
    }.flatMapFirst {
        locationStateProvider.locationStates.notNullLatLng.take(1)
    }.flatMapFirst { latLng -> loadingEventsUpdates(latLng, currentState) }

    private val Flow<LocationState>.notNullLatLng get() = map { it.latLng }.filterNotNull()

    private fun loadingEventsUpdates(
        latLng: LatLng,
        currentState: () -> NearbyState
    ): Flow<NearbyStateUpdate> = getPagedEventsFlow(
        currentEvents = currentState().events,
        toEvent = { selectable -> selectable.item }
    ) { offset ->
        getNearbyEvents(latLng.latitude, latLng.longitude, offset)
    }.map { resource ->
        NearbyStateUpdate.Events.Loaded(resource)
    }.onStart<NearbyStateUpdate> { emit(NearbyStateUpdate.Events.Loading) }

    private fun Flow<NearbyIntent.AddToFavouritesClicked>.addToFavouritesUpdates(
        coroutineScope: CoroutineScope,
        currentState: () -> NearbyState,
        intent: suspend (NearbyIntent) -> Unit,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = map {
        val selectedEvents = currentState().events.data.filter { it.selected }.map { it.item }
        withContext(ioDispatcher) { saveEvents(selectedEvents) }
        signal(NearbySignal.FavouritesSaved)
        NearbyStateUpdate.Events.AddedToFavourites(
            snackbarText = addedToFavouritesMessage(eventsCount = selectedEvents.size),
            onSnackbarDismissed = { coroutineScope.launch { intent(NearbyIntent.HideSnackbar) } }
        )
    }
}
