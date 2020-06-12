package com.example.search

import androidx.lifecycle.SavedStateHandle
import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.provider.ConnectedStateProvider
import com.example.core.usecase.*
import com.example.core.util.Loading
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.FlowProcessor
import com.example.coreandroid.util.addedToFavouritesMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class SearchFlowProcessor @Inject constructor(
    private val searchEvents: SearchEvents,
    private val getPagedEventsFlow: GetPagedEventsFlow,
    private val saveEvents: SaveEvents,
    private val getSearchSuggestions: GetSearchSuggestions,
    private val saveSearchSuggestion: SaveSearchSuggestion,
    private val connectedStateProvider: ConnectedStateProvider,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<SearchIntent, SearchStateUpdate, SearchState, SearchSignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<SearchIntent>,
        currentState: () -> SearchState,
        states: StateFlow<SearchState>,
        intent: suspend (SearchIntent) -> Unit,
        signal: suspend (SearchSignal) -> Unit,
        savedStateHandle: SavedStateHandle
    ): Flow<SearchStateUpdate> = merge(
        intents.updates(coroutineScope, currentState, intent, signal),
        connectedStateProvider.updates(currentState)
    )

    private fun Flow<SearchIntent>.updates(
        coroutineScope: CoroutineScope,
        currentState: () -> SearchState,
        intent: suspend (SearchIntent) -> Unit,
        signal: suspend (SearchSignal) -> Unit
    ): Flow<SearchStateUpdate> = merge(
        filterIsInstance<SearchIntent.NewSearch>()
            .newSearchUpdates(currentState),
        filterIsInstance<SearchIntent.LoadMoreResults>()
            .loadMoreResultsUpdates(currentState),
        filterIsInstance<SearchIntent.ClearSelectionClicked>()
            .map { SearchStateUpdate.ClearSelection },
        filterIsInstance<SearchIntent.EventLongClicked>()
            .map { SearchStateUpdate.ToggleEventSelection(it.event) },
        filterIsInstance<SearchIntent.HideSnackbar>()
            .map { SearchStateUpdate.HideSnackbar },
        filterIsInstance<SearchIntent.AddToFavouritesClicked>()
            .addToFavouritesUpdates(coroutineScope, currentState, intent, signal)
    )

    private fun ConnectedStateProvider.updates(
        currentState: () -> SearchState
    ): Flow<SearchStateUpdate> = connectedStates.filter { connected ->
        currentState().run { connected && events.loadingFailed && events.data.isEmpty() }
    }.flatMapFirst {
        searchEventsUpdates(
            newSearch = true,
            startWithLoading = false,
            currentState = currentState
        )
    }

    private fun Flow<SearchIntent.NewSearch>.newSearchUpdates(
        currentState: () -> SearchState
    ): Flow<SearchStateUpdate> = onEach { (text, shouldSave) ->
        if (shouldSave) saveSearchSuggestion(text)
    }.distinctUntilChangedBy {
        it.text
    }.flatMapLatest { (text) ->
        flow<SearchStateUpdate> {
            emit(SearchStateUpdate.Events.Loading(searchText = text))
            val suggestions = withContext(ioDispatcher) { getSearchSuggestions(text) }
            emit(SearchStateUpdate.Suggestions(suggestions))
        }.onCompletion {
            emitAll(
                searchEventsUpdates(
                    newSearch = true,
                    startWithLoading = false,
                    currentState = currentState
                )
            )
        }
    }

    private fun Flow<SearchIntent.LoadMoreResults>.loadMoreResultsUpdates(
        currentState: () -> SearchState
    ): Flow<SearchStateUpdate> = filterNot {
        val events = currentState().events
        events.status is Loading || !events.canLoadMore || events.data.isEmpty()
    }.flatMapFirst {
        searchEventsUpdates(
            newSearch = false,
            startWithLoading = true,
            currentState = currentState
        )
    }

    private fun searchEventsUpdates(
        newSearch: Boolean,
        startWithLoading: Boolean,
        currentState: () -> SearchState
    ): Flow<SearchStateUpdate> = currentState().let { startState ->
        getPagedEventsFlow(
            currentEvents = startState.events,
            toEvent = { selectable -> selectable.item }
        ) { offset ->
            searchEvents(
                searchText = startState.searchText,
                offset = if (newSearch) null else offset
            )
        }.map<Resource<PagedResult<IEvent>>, SearchStateUpdate> {
            SearchStateUpdate.Events.Loaded(it, newSearch)
        }.run {
            if (startWithLoading) onStart { emit(SearchStateUpdate.Events.Loading()) }
            else this
        }
    }

    private fun Flow<SearchIntent.AddToFavouritesClicked>.addToFavouritesUpdates(
        coroutineScope: CoroutineScope,
        currentState: () -> SearchState,
        intent: suspend (SearchIntent) -> Unit,
        signal: suspend (SearchSignal) -> Unit
    ): Flow<SearchStateUpdate> = map {
        val selectedEvents = currentState().events.data.filter { it.selected }.map { it.item }
        withContext(ioDispatcher) { saveEvents(selectedEvents) }
        signal(SearchSignal.FavouritesSaved)
        SearchStateUpdate.Events.AddedToFavourites(
            snackbarText = addedToFavouritesMessage(eventsCount = selectedEvents.size),
            onSnackbarDismissed = { coroutineScope.launch { intent(SearchIntent.HideSnackbar) } }
        )
    }
}
