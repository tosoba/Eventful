package com.example.nearby

import androidx.lifecycle.LifecycleOwner
import com.example.coreandroid.arch.state.PagedAsyncData
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.util.LocationState
import com.example.coreandroid.util.observeUsing
import com.shopify.livedataktx.distinct
import com.shopify.livedataktx.filter
import com.shopify.livedataktx.map
import com.shopify.livedataktx.nonNull
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FragmentScoped
class NearbyViewEventHandler @Inject constructor(
    private val viewModel: NearbyViewModel,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val locationStateProvider: LocationStateProvider
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + trackerJob

    private val trackerJob = Job()

    private val viewEventsChannel: Channel<NearbyViewEvent> = Channel()
    val viewEventsSendChannel: SendChannel<NearbyViewEvent> = viewEventsChannel

    private val viewUpdatesChannel: Channel<NearbyViewAction> = Channel()
    val viewUpdatesReceiveChannel: ReceiveChannel<NearbyViewAction> = viewUpdatesChannel

    private val events: PagedAsyncData<EventUiModel> get() = viewModel.viewStateObservable.currentState.events

    init {
        launch {
            viewEventsChannel.consumeEach {
                when (it) {
                    is Interaction.EventListScrolledToEnd -> checkConditionsAndLoadEvents()
                    is Interaction.EventClicked -> onEventClicked(it.event)
                    is Lifecycle.OnViewCreated -> onViewCreated(it.lifecycleOwner, it.wasRecreated)
                    is Lifecycle.OnDestroy -> onDestroy()
                }
            }
        }
    }

    private fun onEventClicked(event: EventUiModel) {
        viewUpdatesChannel.offer(ShowEvent(event))
    }

    private fun onViewCreated(owner: LifecycleOwner, wasRecreated: Boolean) {
        viewModel.viewStateObservable.observe(owner) {
            if (it.events.lastLoadingStatus == PagedAsyncData.LoadingStatus.CompletedSuccessfully) {
                viewUpdatesChannel.offer(UpdateEvents(it.events.items))
            }

            if (it.events.lastLoadingStatus == PagedAsyncData.LoadingStatus.InProgress) {
                viewUpdatesChannel.offer(ShowLoadingSnackbar)
            }
        }

        viewModel.viewStateObservable.liveState
            .nonNull()
            .filter { state: NearbyState ->
                state.events.lastLoadingFailed &&
                        (state.events.lastLoadingStatus as PagedAsyncData.LoadingStatus.CompletedWithError).throwable is NearbyError
            }
            .map { state: NearbyState ->
                (state.events.lastLoadingStatus as PagedAsyncData.LoadingStatus.CompletedWithError).throwable as NearbyError
            }
            .observeUsing(owner) {
                when (it) {
                    is NearbyError.NotConnected -> viewUpdatesChannel.offer(ShowNoConnectionMessage)
                    is NearbyError.LocationUnavailable -> viewUpdatesChannel.offer(
                        ShowLocationUnavailableMessage
                    )
                }
            }

        connectivityStateProvider.isConnectedLive
            .distinct()
            .observeUsing(owner) {
                if (it && events.emptyAndLastLoadingFailed) {
                    val locationState = locationStateProvider.locationState
                    if (locationState is LocationState.Found) {
                        events.doIfEmptyAndLoadingNotInProgress {
                            viewModel.loadEvents(locationState.latLng)
                        }
                    } else {
                        viewUpdatesChannel.offer(ShowLocationUnavailableMessage)
                    }
                }
            }

        locationStateProvider.locationStateLive
            .distinct()
            .observeUsing(owner) { locationState ->
                if (locationState is LocationState.Found && events.items.isEmpty()) {
                    if (connectivityStateProvider.isConnected) {
                        events.doIfEmptyAndLoadingNotInProgress {
                            viewModel.loadEvents(locationState.latLng)
                            //TODO: this is here just for testing
                            viewModel.loadTicketMasterEvents(locationState.latLng)
                        }
                    } else {
                        viewUpdatesChannel.offer(ShowNoConnectionMessage)
                    }
                } else if (locationState is LocationState.Loading) {
                    viewUpdatesChannel.offer(ShowLoadingSnackbar)
                }
            }

        if (!wasRecreated && events.items.isEmpty()) {
            checkConditionsAndLoadEvents()
        }
    }

    private fun onDestroy() {
        viewEventsChannel.cancel()
        viewUpdatesChannel.cancel()
        trackerJob.cancel()
    }

    private fun checkConditionsAndLoadEvents() {
        val locationState = locationStateProvider.locationState
        if (locationState !is LocationState.Found) {
            if (locationState !is LocationState.Loading) viewModel.onLocationUnavailable()
            return
        }

        if (!connectivityStateProvider.isConnected) {
            viewModel.onNotConnected()
            return
        }

        events.doIfLoadingNotInProgressAndNotAllLoaded {
            viewModel.loadEvents(locationState.latLng)
            //TODO: this is here just for testing
            viewModel.loadTicketMasterEvents(locationState.latLng)
        }
    }
}