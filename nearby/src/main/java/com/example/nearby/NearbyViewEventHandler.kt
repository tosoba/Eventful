package com.example.nearby

import androidx.lifecycle.LifecycleOwner
import com.example.coreandroid.arch.state.PagedAsyncData
import com.example.coreandroid.main.MainViewModel
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.util.observe
import com.snakydesign.livedataextensions.distinctUntilChanged
import com.snakydesign.livedataextensions.map
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class NearbyViewEventHandler(
    private val viewModel: NearbyViewModel,
    private val mainViewModel: MainViewModel
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + trackerJob

    private val trackerJob = Job()

    private val viewEventsChannel: Channel<NearbyViewEvent> = Channel()
    val viewEventsSendChannel: SendChannel<NearbyViewEvent> = viewEventsChannel

    private val viewUpdatesChannel: Channel<NearbyViewAction> = Channel()
    val viewUpdatesReceiveChannel: ReceiveChannel<NearbyViewAction> = viewUpdatesChannel

    init {
        launch {
            viewEventsChannel.consumeEach {
                when (it) {
                    is Interaction.EventListScrolledToEnd -> checkConditionsAndLoadEvents()
                    is Interaction.EventClicked -> onEventClicked(it.event)
                    is Lifecycle.OnViewCreated -> onViewCreated(it.lifecycleOwner)
                    is Lifecycle.OnDestroy -> onDestroy()
                }
            }
        }
    }

    private fun onEventClicked(event: EventUiModel) {
        viewUpdatesChannel.offer(ShowEvent(event))
    }

    private fun onViewCreated(owner: LifecycleOwner) {
        viewModel.viewStateStore.observe(owner) {
            if (it.events.lastLoadingStatus == PagedAsyncData.LoadingStatus.CompletedSuccessfully) {
                viewUpdatesChannel.offer(UpdateEvents(it.events.items))
            }
        }

        mainViewModel.viewStateStore.liveState
            .map { it.isConnected }
            .distinctUntilChanged()
            .observe(owner) {
                if (it && viewModel.viewStateStore.currentState.events.lastLoadingFailed) {
                    //TODO: check if location available
                    loadEvents()
                }
            }

        checkConditionsAndLoadEvents()
    }

    private fun onDestroy() {
        viewEventsChannel.cancel()
        viewUpdatesChannel.cancel()
        trackerJob.cancel()
    }

    private fun loadEvents() {
        viewModel.viewStateStore.currentState.events.doIfEmptyAndLoadingNotInProgress {
            viewModel.loadEvents()
        }
    }

    private fun checkConditionsAndLoadEvents() {
        //TODO: check location first then check isConnected in else if
        if (mainViewModel.viewStateStore.currentState.isConnected) {
            loadEvents()
        } else {
            viewModel.onNotConnected()
        }
    }
}