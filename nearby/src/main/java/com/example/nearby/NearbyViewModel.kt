package com.example.nearby

import android.view.View
import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.app.LatLng
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.model.ticketmaster.IEvent
import com.example.core.model.ticketmaster.trimmedLowerCasedName
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseStateFlowViewModel
import com.example.coreandroid.controller.SnackbarAction
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.*
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


@ExperimentalCoroutinesApi
@FlowPreview
class NearbyViewModel(
    private val getNearbyEvents: GetNearbyEvents,
    private val saveEvents: SaveEvents,
    connectedStateProvider: ConnectedStateProvider,
    private val locationStateProvider: LocationStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: NearbyState = NearbyState()
) : BaseStateFlowViewModel<NearbyIntent, NearbyState, NearbySignal>(initialState) {

    init {
        merge(
            intents.updates,
            connectedStateProvider.updates,
            locationStateProvider.locationUpdates,
            locationStateProvider.snackbarUpdates
        ).applyToState(initialState = initialState)
    }

    private val Flow<NearbyIntent>.updates: Flow<Update>
        get() = merge(
            filterIsInstance<ClearSelectionClicked>().map { Update.ClearSelection },
            filterIsInstance<EventLongClicked>().map { Update.ToggleEventSelection(it.event) },
            filterIsInstance<HideSnackbar>().map { Update.HideSnackbar },
            filterIsInstance<LoadMoreResults>().loadMoreResultsUpdates,
            filterIsInstance<AddToFavouritesClicked>().addToFavouritesUpdates
        )

    private val ConnectedStateProvider.updates: Flow<Update>
        get() = connectedStates.filter { connected ->
            state.run { connected && events.loadingFailed && events.data.isEmpty() }
        }.flatMapFirst {
            locationStateProvider.locationStates.notNullLatLng.take(1)
        }.flatMapLatest { latLng -> loadingEventsUpdates(latLng) }

    private val LocationStateProvider.snackbarUpdates: Flow<Update> //TODO: zip this with connection status?
        get() = locationStates
            .filter { it.latLng == null }
            .map { (_, status) ->
                Update.LocationSnackbar(status, locationStateProvider::reloadLocation)
            }
            .filterNotNull()

    private val LocationStateProvider.locationUpdates: Flow<Update>
        get() = locationStates.notNullLatLng
            .filter { state.events.data.isEmpty() } //TODO: this won't work with refreshing with SwipeRefreshLayout
            .flatMapLatest { latLng -> loadingEventsUpdates(latLng) }

    private val Flow<LoadMoreResults>.loadMoreResultsUpdates: Flow<Update>
        get() = filterNot {
            val events = state.events
            events.status is Loading || !events.canLoadMore || events.data.isEmpty()
        }.flatMapFirst {
            locationStateProvider.locationStates.notNullLatLng.take(1)
        }.flatMapFirst { latLng -> loadingEventsUpdates(latLng) }

    private val Flow<LocationState>.notNullLatLng get() = map { it.latLng }.filterNotNull()

    private fun loadingEventsUpdates(latLng: LatLng): Flow<Update> = pagedEventsFlow(
        currentEvents = state.events,
        dispatcher = ioDispatcher,
        toEvent = { selectable -> selectable.item },
        getEvents = { offset -> getNearbyEvents(latLng.lat, latLng.lng, offset) }
    ).map { resource -> Update.Events.Loaded(resource) }
        .onStart<Update> { emit(Update.Events.Loading) }

    private val Flow<AddToFavouritesClicked>.addToFavouritesUpdates: Flow<Update>
        get() = map {
            val selectedEvents = state.events.data.filter { it.selected }.map { it.item }
            withContext(ioDispatcher) { saveEvents(selectedEvents) }
            signal(NearbySignal.FavouritesSaved)
            Update.Events.AddedToFavourites(selectedEvents.size) {
                viewModelScope.launch { signal(NearbySignal.FavouritesSaved) }
            }
        }

    private sealed class Update : StateUpdate<NearbyState> {
        class ToggleEventSelection(
            override val event: Event
        ) : Update(),
            ToggleEventSelectionUpdate<NearbyState>

        object ClearSelection : Update(), ClearSelectionUpdate<NearbyState>

        class LocationSnackbar(
            private val status: LocationStatus,
            private val reloadLocation: () -> Unit
        ) : Update() {
            override fun invoke(state: NearbyState): NearbyState = when (status) {
                is LocationStatus.PermissionDenied -> state.copy(
                    snackbarState = SnackbarState.Shown("No location permission")
                )
                is LocationStatus.Disabled -> state.copy(
                    snackbarState = SnackbarState.Shown("Location disabled")
                )
                is LocationStatus.Loading -> state.copy(
                    snackbarState = SnackbarState.Shown("Loading location...")
                )
                is LocationStatus.Error -> state.copy(
                    snackbarState = SnackbarState.Shown(
                        "Unable to load location - error occurred",
                        action = SnackbarAction(
                            "Retry",
                            View.OnClickListener { reloadLocation() }
                        )
                    )
                )
                else -> state
            }
        }

        object HideSnackbar : Update() {
            override fun invoke(state: NearbyState): NearbyState = state
                .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
        }

        sealed class Events : Update() {
            object Loading : Events() {
                override fun invoke(state: NearbyState): NearbyState = state.copy(
                    events = state.events.copyWithLoadingStatus
                )
            }

            //TODO: refactor this
            class Loaded(private val resource: Resource<PagedResult<IEvent>>) : Update() {
                override fun invoke(state: NearbyState): NearbyState = state.run {
                    when (resource) {
                        is Resource.Success -> copy(
                            events = events.copyWithNewItemsDistinct(
                                resource.data.items.map { Selectable(Event(it)) },
                                resource.data.currentPage + 1,
                                resource.data.totalPages
                            ) { (event, _) -> event.trimmedLowerCasedName },
                            snackbarState = SnackbarState.Hidden
                        )

                        is Resource.Error<PagedResult<IEvent>> -> copy(
                            events = events.copyWithFailureStatus(resource.error),
                            snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
                                if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504)
                                    SnackbarState.Shown("No connection")
                                else SnackbarState.Shown("Unknown network error")
                            } else snackbarState
                        )
                    }
                }
            }

            class AddedToFavourites(
                override val addedCount: Int,
                override val onDismissed: () -> Unit
            ) : Update(),
                AddedToFavouritesUpdate<NearbyState>
        }
    }
}
