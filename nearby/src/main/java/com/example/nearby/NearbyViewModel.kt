package com.example.nearby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LatLng
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.provider.ConnectedStateProvider
import com.example.core.provider.LocationStateProvider
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.usecase.SaveEvents
import com.example.core.util.Loading
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.util.addedToFavouritesMessage
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class NearbyViewModel @AssistedInject constructor(
    private val getNearbyEvents: GetNearbyEvents,
    private val saveEvents: SaveEvents,
    private val getPagedEventsFlow: GetPagedEventsFlow,
    private val connectedStateProvider: ConnectedStateProvider,
    private val locationStateProvider: LocationStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<NearbyIntent, NearbyStateUpdate, NearbyState, NearbySignal>(
    savedStateHandle["initialState"] ?: NearbyState()
) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<NearbyViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): NearbyViewModel
    }

    init {
        start()
    }

    override val updates: Flow<NearbyStateUpdate>
        get() = merge(
            intents.updates,
            connectedStateProvider.updates,
            connectedStateProvider.snackbarUpdates,
            locationStateProvider.updates,
            locationStateProvider.snackbarUpdates
        )

    private val Flow<NearbyIntent>.updates: Flow<NearbyStateUpdate>
        get() = merge(
            filterIsInstance<NearbyIntent.ClearSelectionClicked>()
                .map { NearbyStateUpdate.ClearSelection },
            filterIsInstance<NearbyIntent.EventLongClicked>()
                .map { NearbyStateUpdate.ToggleEventSelection(it.event) },
            filterIsInstance<NearbyIntent.HideSnackbar>()
                .map { NearbyStateUpdate.HideSnackbar },
            filterIsInstance<NearbyIntent.LoadMoreResults>().loadMoreResultsUpdates,
            filterIsInstance<NearbyIntent.AddToFavouritesClicked>().addToFavouritesUpdates
        )

    private val ConnectedStateProvider.updates: Flow<NearbyStateUpdate>
        get() = connectedStates.filter { connected ->
            state.run { connected && events.loadingFailed && events.data.isEmpty() }
        }.flatMapFirst {
            locationStateProvider.locationStates.notNullLatLng.take(1)
        }.flatMapLatest { latLng -> loadingEventsUpdates(latLng) }

    private val ConnectedStateProvider.snackbarUpdates: Flow<NearbyStateUpdate>
        get() = connectedStates.filter { connected ->
            state.run { !connected && events.loadingFailed }
        }.flatMapFirst {
            locationStateProvider.locationStates.notNullLatLng.take(1)
        }.map { NearbyStateUpdate.NoConnectionSnackbar }

    private val LocationStateProvider.snackbarUpdates: Flow<NearbyStateUpdate>
        get() = locationStates.filter { it.latLng == null && it.status !is LocationStatus.Initial }
            .map { (_, status) ->
                NearbyStateUpdate.LocationSnackbar(status, locationStateProvider::reloadLocation)
            }

    private val LocationStateProvider.updates: Flow<NearbyStateUpdate>
        get() = locationStates.notNullLatLng
            .filter { state.events.data.isEmpty() } //TODO: this won't work with refreshing with SwipeRefreshLayout
            .flatMapLatest { latLng -> loadingEventsUpdates(latLng) }

    private val Flow<NearbyIntent.LoadMoreResults>.loadMoreResultsUpdates: Flow<NearbyStateUpdate>
        get() = filterNot {
            val events = state.events
            events.status is Loading || !events.canLoadMore || events.data.isEmpty()
        }.flatMapFirst {
            locationStateProvider.locationStates.notNullLatLng.take(1)
        }.flatMapFirst { latLng -> loadingEventsUpdates(latLng) }

    private val Flow<LocationState>.notNullLatLng get() = map { it.latLng }.filterNotNull()

    private fun loadingEventsUpdates(latLng: LatLng): Flow<NearbyStateUpdate> = getPagedEventsFlow(
        currentEvents = state.events,
        toEvent = { selectable -> selectable.item }
    ) { offset ->
        getNearbyEvents(latLng.lat, latLng.lng, offset)
    }.map { resource ->
        NearbyStateUpdate.Events.Loaded(resource)
    }.onStart<NearbyStateUpdate> { emit(NearbyStateUpdate.Events.Loading) }

    private val Flow<NearbyIntent.AddToFavouritesClicked>.addToFavouritesUpdates: Flow<NearbyStateUpdate>
        get() = map {
            val selectedEvents = state.events.data.filter { it.selected }.map { it.item }
            withContext(ioDispatcher) { saveEvents(selectedEvents) }
            signal(NearbySignal.FavouritesSaved)
            NearbyStateUpdate.Events.AddedToFavourites(
                snackbarText = addedToFavouritesMessage(eventsCount = selectedEvents.size),
                onSnackbarDismissed = { viewModelScope.launch { intent(NearbyIntent.HideSnackbar) } }
            )
        }
}
