package com.example.nearby

import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.usecase.SaveEvents
import com.example.core.util.Loading
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.FlowProcessor
import com.example.coreandroid.base.addedToFavouritesMessage
import com.example.coreandroid.model.location.LocationState
import com.example.coreandroid.model.location.LocationStatus
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
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
        states: Flow<NearbyState>,
        intent: suspend (NearbyIntent) -> Unit,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = merge(
        intents
            .onEach {
                if (it is NearbyIntent.ReloadLocation) {
                    if (currentState().events.data.isNotEmpty()) locationStateProvider.reloadLocation()
                    else signal(NearbySignal.EventsLoadingFinished)
                }
            }
            .updates(coroutineScope, currentState, intent, signal),
        connectedStateProvider.updates(currentState, signal),
        connectedStateProvider.snackbarUpdates(currentState),
        locationStateProvider.updates(currentState, signal),
        locationStateProvider.snackbarUpdates(signal)
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
            .loadMoreResultsUpdates(currentState, signal),
        filterIsInstance<NearbyIntent.AddToFavouritesClicked>()
            .addToFavouritesUpdates(coroutineScope, currentState, intent, signal)
    )

    private fun ConnectedStateProvider.updates(
        currentState: () -> NearbyState,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = connectedStates.filter { connected ->
        currentState().run { connected && events.loadingFailed && events.data.isEmpty() }
    }.flatMapFirst {
        locationStateProvider.locationStates.notNullLatLng.take(1)
    }.flatMapLatest { latLng -> loadingEventsUpdates(latLng, false, currentState, signal) }

    private fun ConnectedStateProvider.snackbarUpdates(
        currentState: () -> NearbyState
    ): Flow<NearbyStateUpdate> = connectedStates.filter { connected ->
        currentState().run { !connected && events.loadingFailed }
    }.flatMapFirst {
        locationStateProvider.locationStates.notNullLatLng.take(1)
    }.map { NearbyStateUpdate.NoConnectionSnackbar }

    private fun LocationStateProvider.snackbarUpdates(
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = locationStates
        .filter { (_, status) -> status != LocationStatus.Initial && status != LocationStatus.Found }
        .map { (latLng, status) ->
            if (status !is LocationStatus.Loading) signal(NearbySignal.EventsLoadingFinished)
            NearbyStateUpdate.LocationSnackbar(
                latLng,
                status,
                locationStateProvider::reloadLocation
            )
        }

    private fun LocationStateProvider.updates(
        currentState: () -> NearbyState,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = locationStates
        .filter { (_, status) -> status is LocationStatus.Found }
        .map { it.latLng }
        .filterNotNull()
        .distinctUntilChanged()
        .filterNot { currentState().events.loadingFailed }
        .flatMapLatest { latLng -> loadingEventsUpdates(latLng, true, currentState, signal) }

    private fun Flow<NearbyIntent.LoadMoreResults>.loadMoreResultsUpdates(
        currentState: () -> NearbyState,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = filterNot {
        val events = currentState().events
        events.status is Loading || !events.canLoadMore || events.data.isEmpty()
    }.flatMapFirst {
        locationStateProvider.locationStates.notNullLatLng.take(1)
    }.flatMapFirst { latLng -> loadingEventsUpdates(latLng, false, currentState, signal) }

    private val Flow<LocationState>.notNullLatLng get() = map { it.latLng }.filterNotNull()

    private fun loadingEventsUpdates(
        latLng: LatLng,
        newLocation: Boolean,
        currentState: () -> NearbyState,
        signal: suspend (NearbySignal) -> Unit
    ): Flow<NearbyStateUpdate> = getPagedEventsFlow(
        currentEvents = currentState().events,
        toEvent = { selectable -> selectable.item }
    ) { offset ->
        getNearbyEvents(latLng.latitude, latLng.longitude, offset = if (newLocation) 0 else offset)
    }.map { resource ->
        signal(NearbySignal.EventsLoadingFinished)
        NearbyStateUpdate.Events.Loaded(resource, clearEventsIfSuccess = newLocation)
    }.onStart<NearbyStateUpdate> { emit(NearbyStateUpdate.Events.Loading(newLocation)) }

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
