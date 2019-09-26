package com.example.search

import android.content.Context
import com.example.coreandroid.base.ConnectivityStateProvider
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
class SearchViewEventHandler @Inject constructor(
    private val appContext: Context,
    val viewModel: SearchViewModel,
    private val connectivityStateProvider: ConnectivityStateProvider
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + trackerJob

    private val trackerJob = Job()

    private val viewUpdatesChannel: Channel<SearchViewUpdate> =
        Channel(capacity = Channel.UNLIMITED)

    private val events: PagedDataList<Event> get() = viewModel.currentState.events

    private val eventsActionsFlow: Flow<SearchViewUpdate?> by lazy {
        viewModel.state.map { it.events }
            .distinctUntilChanged()
            .map {
                when (val status = it.status) {
                    is LoadedSuccessfully, Loading -> InvalidateList(true)
                    is LoadingFailed<*> -> when (val error = status.error) {
                        is SearchError -> when (error) {
                            is SearchError.NotConnected -> ShowSnackbarAndInvalidateList(
                                appContext.getString(R.string.no_connection), true
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

    private val connectionStateActionsFlow: Flow<SearchViewUpdate?> by lazy {
        connectivityStateProvider.isConnectedFlow
            .distinctUntilChanged()
            .filter { it && events.loadingFailed }
            .onEach {
                events.ifEmptyAndIsNotLoading {
                    viewModel.search(viewModel.currentState.searchText)
                }
            }
            .map { null }
    }

    val updates: Flow<SearchViewUpdate> by lazy {
        flowOf(
            eventsActionsFlow,
            connectionStateActionsFlow,
            viewUpdatesChannel.consumeAsFlow()
        ).flattenMerge().filterNotNull()
    }

    private val eventProcessor = actor<SearchViewEvent>(
        context = coroutineContext,
        capacity = Channel.UNLIMITED
    ) {
        consumeEach {
            when (it) {
                is Interaction.SearchTextChanged -> trySearch(it.searchText)
                is Interaction.EventListScrolledToEnd -> trySearchForMore()
                is Interaction.EventClicked -> viewUpdatesChannel.offer(ShowEvent(it.event))
                is Lifecycle.OnDestroy -> onDestroy()
            }
        }
    }

    private val loadingEventsFailedWithNetworkError: Boolean
        get() {
            val status = events.status as? LoadingFailed<*> ?: return false
            return status.error is SearchError.NotConnected
                    || status.error is NetworkResponse.ServerError<*>
                    || status.error is NetworkResponse.NetworkError
        }

    fun eventOccurred(event: SearchViewEvent) = eventProcessor.offer(event)

    private fun trySearch(searchText: String) = checkConnectionAndRun {
        viewModel.search(searchText)
    }

    private fun trySearchForMore() = checkConnectionAndRun { viewModel.searchMore() }

    private fun checkConnectionAndRun(block: () -> Unit) {
        if (!connectivityStateProvider.isConnected && loadingEventsFailedWithNetworkError) {
            viewModel.onNotConnected()
            return
        }

        block()
    }

    private fun onDestroy() {
        eventProcessor.close()
        viewUpdatesChannel.cancel()
        trackerJob.cancel()
    }
}