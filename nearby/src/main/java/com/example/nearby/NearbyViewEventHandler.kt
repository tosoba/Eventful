package com.example.nearby

import android.content.Context
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.ticketmaster.Event
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

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + trackerJob

    private val trackerJob = Job()

    private val viewUpdatesChannel: Channel<NearbyViewUpdate> =
        Channel(capacity = Channel.UNLIMITED)

    private val events: PagedDataList<Event> get() = viewModel.currentState.events

    private val eventsActionsFlow: Flow<NearbyViewUpdate?> by lazy {
        viewModel.state.map { it.events }
            .distinctUntilChanged()
            .map {
                when (val status = it.status) {
                    is LoadedSuccessfully, Loading -> InvalidateList(true)
                    is LoadingFailed<*> -> when (val error = status.error) {
                        is NearbyError -> when (error) {
                            is NearbyError.NotConnected -> ShowSnackbarAndInvalidateList(
                                appContext.getString(R.string.no_connection), true
                            )
                            is NearbyError.LocationUnavailable -> ShowSnackbarAndInvalidateList(
                                appContext.getString(R.string.unable_to_retrieve_location), true
                            )
                            is NearbyError.LocationNotLoadedYet -> ShowSnackbarAndInvalidateList(
                                appContext.getString(R.string.retrieving_location), true
                            )
                        }
                        is NetworkResponse.ServerError<*> -> {
                            if (error.code in 503..504) ShowSnackbarAndInvalidateList(
                                appContext.getString(R.string.no_connection), true
                            )
                            else ShowSnackbarAndInvalidateList(
                                appContext.getString(R.string.unknown_network_error), true
                            )
                        }
                        else -> null
                    }
                    is Initial -> null
                }
            }
    }

    private val connectionStateActionsFlow: Flow<NearbyViewUpdate?> by lazy {
        connectivityStateProvider.isConnectedFlow
            .distinctUntilChanged()
            .filter { it && events.loadingFailed }
            .onEach {
                val locationState = locationStateProvider.locationState
                if (locationState is LocationState.Found) events.ifEmptyAndIsNotLoading {
                    viewModel.loadEvents(locationState.latLng)
                }
            }
            .map {
                val locationState = locationStateProvider.locationState
                if (locationState !is LocationState.Found && locationState !is LocationState.Loading) {
                    ShowSnackbarAndInvalidateList(
                        appContext.getString(R.string.unable_to_retrieve_location), true
                    )
                } else null
            }
    }

    private val locationStateActionsFlow: Flow<NearbyViewUpdate?> by lazy {
        locationStateProvider.locationStateFlow
            .distinctUntilChanged()
            .filter { events.loadingFailed }
            .onEach { locationState ->
                if (locationState is LocationState.Found) events.ifEmptyAndIsNotLoading {
                    viewModel.loadEvents(locationState.latLng)
                }
            }
            .map { locationState ->
                if (locationState is LocationState.Found && !connectivityStateProvider.isConnected) {
                    ShowSnackbarAndInvalidateList(
                        appContext.getString(R.string.no_connection), true
                    )
                } else if (locationState is LocationState.Loading) {
                    ShowSnackbarAndInvalidateList(
                        appContext.getString(R.string.retrieving_location), false
                    )
                } else null
            }
    }

    val updates: Flow<NearbyViewUpdate> by lazy {
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

    private val loadingEventsFailedWithNetworkError: Boolean
        get() {
            val status = events.status as? LoadingFailed<*> ?: return false
            return status.error is NearbyError.NotConnected
                    || status.error is NetworkResponse.ServerError<*>
                    || status.error is NetworkResponse.NetworkError
        }

    fun eventOccurred(event: NearbyViewEvent) = eventProcessor.offer(event)

    private fun onViewCreated(wasRecreated: Boolean) {
        if (!wasRecreated && events.value.isEmpty()) loadEventsIfPossible()
    }

    private fun onDestroy() {
        eventProcessor.close()
        viewUpdatesChannel.cancel()
        trackerJob.cancel()
    }

    private fun loadEventsIfPossible() {
        if (!connectivityStateProvider.isConnected && loadingEventsFailedWithNetworkError) {
            viewModel.onNotConnected()
            return
        }

        val locationState = locationStateProvider.locationState
        if (locationState is LocationState.Loading) {
            viewModel.onLocationNotLoadedYet()
            return
        } else if (locationState !is LocationState.Found) {
            viewModel.onLocationUnavailable()
            return
        }

        events.ifNotLoadingAndNotAllLoaded { viewModel.loadEvents(locationState.latLng) }
    }
}