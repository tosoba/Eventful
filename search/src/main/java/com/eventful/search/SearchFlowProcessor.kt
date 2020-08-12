package com.eventful.search

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowProcessor
import com.eventful.core.android.base.addedToFavouritesMsgRes
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.provider.ConnectedStateProvider
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.usecase.event.SaveEvents
import com.eventful.core.usecase.event.SearchEvents
import com.eventful.core.usecase.search.GetSearchSuggestions
import com.eventful.core.usecase.search.SaveSearchSuggestion
import com.eventful.core.util.Loading
import com.eventful.core.util.ext.flatMapFirst
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class SearchFlowProcessor @Inject constructor(
    private val searchEvents: SearchEvents,
    private val getPagedEvents: GetPagedEvents,
    private val saveEvents: SaveEvents,
    private val getSearchSuggestions: GetSearchSuggestions,
    private val saveSearchSuggestion: SaveSearchSuggestion,
    private val connectedStateProvider: ConnectedStateProvider,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<SearchIntent, SearchStateUpdate, SearchState, SearchSignal> {

    override fun stateWillUpdate(
        currentState: SearchState,
        nextState: SearchState,
        update: SearchStateUpdate,
        savedStateHandle: SavedStateHandle
    ) {
        if (update is SearchStateUpdate.Events.Loading && update.searchText != null) {
            savedStateHandle[SearchState.KEY_SEARCH_TEXT] = update.searchText
        }
    }

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<SearchIntent>,
        currentState: () -> SearchState,
        states: Flow<SearchState>,
        intent: suspend (SearchIntent) -> Unit,
        signal: suspend (SearchSignal) -> Unit
    ): Flow<SearchStateUpdate> = merge(
        intents
            .run {
                val initialSearchText = currentState().searchText
                if (initialSearchText.isNotEmpty()) onStart {
                    emit(SearchIntent.NewSearch(initialSearchText, false))
                }
                else this
            }
            .updates(coroutineScope, currentState, intent, signal),
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
        connected && currentState().items.run { loadingFailed && data.isEmpty() }
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
        flow {
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
        currentState().items.run { status is Loading || !canLoadMore || data.isEmpty() }
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
        flow {
            if (startWithLoading) emit(SearchStateUpdate.Events.Loading())
            val it = getPagedEvents(
                currentEvents = startState.items,
                toEvent = { selectable -> selectable.item }
            ) { offset ->
                searchEvents(
                    searchText = startState.searchText,
                    offset = if (newSearch) null else offset
                )
            }
            emit(SearchStateUpdate.Events.Loaded(it, newSearch))
        }
    }

    private fun Flow<SearchIntent.AddToFavouritesClicked>.addToFavouritesUpdates(
        coroutineScope: CoroutineScope,
        currentState: () -> SearchState,
        intent: suspend (SearchIntent) -> Unit,
        signal: suspend (SearchSignal) -> Unit
    ): Flow<SearchStateUpdate> = map {
        val selectedEvents = currentState().items.data.filter { it.selected }.map { it.item }
        withContext(ioDispatcher) { saveEvents(selectedEvents) }
        signal(SearchSignal.FavouritesSaved)
        SearchStateUpdate.Events.AddedToFavourites(
            msgRes = SnackbarState.Shown.MsgRes(
                addedToFavouritesMsgRes(eventsCount = selectedEvents.size),
                args = arrayOf(selectedEvents.size)
            ),
            onSnackbarDismissed = { coroutineScope.launch { intent(SearchIntent.HideSnackbar) } }
        )
    }
}
