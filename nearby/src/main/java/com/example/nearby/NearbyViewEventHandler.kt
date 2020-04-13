package com.example.nearby

import android.content.Context
import com.example.core.model.app.LocationResult
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.*
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@FragmentScoped
class NearbyViewEventHandler @Inject constructor(
    private val appContext: Context,
    val viewModel: NearbyViewModel,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val locationStateProvider: LocationStateProvider
) : CoroutineScope {

    private val trackerJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + trackerJob

    private val viewUpdatesChannel: Channel<NearbyViewUpdate> = Channel(
        capacity = Channel.UNLIMITED
    )

    private val events: PagedDataList<Selectable<Event>> get() = viewModel.currentState.events

    private val eventsActionsFlow: Flow<NearbyViewUpdate?> by lazy {
        viewModel.state.map { it.events }
            .distinctUntilChanged()
            .map {
                when (val status = it.status) {
                    is LoadedSuccessfully, Loading -> InvalidateList(false)
                    is LoadingFailed<*> -> when (val error = status.error) {
                        is NearbyError -> InvalidateList(true)
                        is NetworkResponse.ServerError<*> -> InvalidateList(true)
                        else -> null
                    }
                    is Initial -> null
                }
            }
    }

    private val loadingConditionsFlow: Flow<NearbyViewUpdate?> by lazy {
        connectivityStateProvider.isConnectedFlow
            .combine(locationStateProvider.locationStateFlow) { connected, locationState -> connected to locationState }
            .distinctUntilChanged()
            .filter { events.loadingFailed }
            .onEach { (_, locationState) ->
                if (locationState is LocationResult.Found) events.ifEmptyAndIsNotLoading {
                    viewModel.loadEvents(locationState.latLng)
                }
            }
            .map { (_, _) -> null }
    }

    private val snackbarStateFlow: Flow<NearbyViewUpdate?> by lazy {
        viewModel.state.map { it.snackarState }.distinctUntilChanged().map { UpdateSnackbar(it) }
    }

    private val signalsFlow: Flow<NearbyViewUpdate?> by lazy {
        viewModel.signalsFlow.map { signal ->
            when (signal) {
                is NearbySignal.FavouritesSaved -> FinishActionModeWithMsg("Favourites saved.")
            }
        }
    }

    val updates: Flow<NearbyViewUpdate> by lazy {
        flowOf(
            eventsActionsFlow,
            loadingConditionsFlow,
            viewUpdatesChannel.consumeAsFlow(),
            snackbarStateFlow,
            signalsFlow
        ).flattenMerge().filterNotNull()
    }

    private val eventProcessor = actor<NearbyViewEvent>(
        context = coroutineContext,
        capacity = Channel.UNLIMITED
    ) {
        consumeEach {
            when (it) {
                is Interaction.EventListScrolledToEnd -> tryLoadEvents()
                is Interaction.EventClicked -> viewUpdatesChannel.offer(ShowEvent(it.event))
                is Interaction.EventLongClicked -> viewModel.toggleEventSelection(it.event)
                is Interaction.ClearSelectionClicked -> viewModel.clearSelection()
                is Interaction.AddToFavouritesClicked -> viewModel.addEventsToFavourites()
                is Lifecycle.OnViewCreated -> onViewCreated(it.wasRecreated)
                is Lifecycle.OnDestroy -> onDestroy()
            }
        }
    }

    private val loadingEventsFailedWithNetworkError: Boolean
        get() {
            val status = events.status as? LoadingFailed<*> ?: return false
            return status.error is NearbyError.NotConnected
                    || status.error is NetworkResponse.ServerError<*>
                    || status.error is NetworkResponse.NetworkError
        }

    fun eventOccurred(event: NearbyViewEvent) {
        eventProcessor.offer(event)
    }

    private fun onViewCreated(wasRecreated: Boolean) {
        if (!wasRecreated && events.value.isEmpty()) tryLoadEvents()
    }

    private fun onDestroy() {
        eventProcessor.close()
        viewUpdatesChannel.cancel()
        trackerJob.cancel()
    }

    private fun tryLoadEvents() {
        if (!connectivityStateProvider.isConnected && loadingEventsFailedWithNetworkError) {
            viewModel.onNotConnected()
            return
        }

        val locationState = locationStateProvider.locationState
        if (locationState is LocationResult.Loading || locationState is LocationResult.Unknown) {
            viewModel.onLocationNotLoadedYet()
            return
        } else if (locationState !is LocationResult.Found) {
            viewModel.onLocationUnavailable()
            return
        }

        viewModel.loadEvents(locationState.latLng)
    }
}