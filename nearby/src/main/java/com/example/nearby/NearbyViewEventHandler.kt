package com.example.nearby

import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.*
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
    val viewModel: NearbyViewModel,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val locationStateProvider: LocationStateProvider
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + trackerJob

    private val trackerJob = Job()

    private val viewUpdatesChannel: Channel<NearbyViewAction> =
        Channel(capacity = Channel.UNLIMITED)

    private val events: PagedDataList<Event> get() = viewModel.currentState.events

    private val eventsActionsFlow: Flow<NearbyViewAction?> by lazy {
        viewModel.state.map { it.events }
            .distinctUntilChanged()
            .map {
                when (it.status) {
                    is LoadedSuccessfully -> UpdateEvents(it.value)
                    is Loading -> ShowLoadingSnackbar
                    is LoadingFailed<*> -> when ((it.status as LoadingFailed<*>).error as NearbyError) {
                        is NearbyError.NotConnected -> ShowNoConnectionMessage
                        is NearbyError.LocationUnavailable -> ShowLocationUnavailableMessage
                    }
                    else -> null
                }
            }
    }

    private val connectionStateActionsFlow: Flow<NearbyViewAction?> by lazy {
        connectivityStateProvider.isConnectedFlow
            .distinctUntilChanged()
            .filter { it && events.isEmptyAndLastLoadingFailed() }
            .onEach {
                val locationState = locationStateProvider.locationState
                if (locationState is LocationState.Found) {
                    events.ifEmptyAndIsNotLoading {
                        viewModel.loadEvents(locationState.latLng)
                    }
                }
            }
            .map {
                if (locationStateProvider.locationState !is LocationState.Found) {
                    ShowLocationUnavailableMessage
                } else null
            }
    }

    private val locationStateActionsFlow: Flow<NearbyViewAction?> by lazy {
        locationStateProvider.locationStateFlow
            .distinctUntilChanged()
            .filter { events.isEmptyAndLastLoadingFailed() }
            .onEach { locationState ->
                if (locationState is LocationState.Found && connectivityStateProvider.isConnected) {
                    events.ifEmptyAndIsNotLoading {
                        viewModel.loadEvents(locationState.latLng)
                    }
                }
            }
            .map { locationState ->
                if (locationState is LocationState.Found && !connectivityStateProvider.isConnected) {
                    ShowNoConnectionMessage
                } else if (locationState is LocationState.Loading) {
                    ShowLoadingSnackbar
                } else null
            }
    }

    val updates: Flow<NearbyViewAction> by lazy {
        flowOf(
            eventsActionsFlow,
            connectionStateActionsFlow,
            locationStateActionsFlow,
            viewUpdatesChannel.consumeAsFlow()
        ).flattenMerge().filterNotNull()
    }

    private val eventProcessor = actor<NearbyViewEvent>(
        context = coroutineContext,
        capacity = Channel.UNLIMITED
    ) {
        consumeEach {
            when (it) {
                is Interaction.EventListScrolledToEnd -> loadEventsIfPossible()
                is Interaction.EventClicked -> viewUpdatesChannel.offer(ShowEvent(it.event))
                is Lifecycle.OnViewCreated -> onViewCreated(it.wasRecreated)
                is Lifecycle.OnDestroy -> onDestroy()
            }
        }
    }

    fun eventOccurred(event: NearbyViewEvent) = eventProcessor.offer(event)

    private fun onViewCreated(wasRecreated: Boolean) {
        if (!wasRecreated && events.value.isEmpty())
            loadEventsIfPossible()
    }

    private fun onDestroy() {
        eventProcessor.close()
        viewUpdatesChannel.cancel()
        trackerJob.cancel()
    }

    private fun loadEventsIfPossible() {
        val locationState = locationStateProvider.locationState
        if (locationState !is LocationState.Found) {
            viewModel.onLocationUnavailable()
            return
        }

        if (!connectivityStateProvider.isConnected) {
            viewModel.onNotConnected()
            return
        }

        events.ifNotLoadingAndNotAllLoaded { viewModel.loadEvents(locationState.latLng) }
    }
}