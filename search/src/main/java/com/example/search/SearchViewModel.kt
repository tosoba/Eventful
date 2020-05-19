package com.example.search

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveEvents
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class SearchViewModel(
    private val searchEvents: SearchEvents,
    private val saveEvents: SaveEvents,
    private val getSearchSuggestions: GetSeachSuggestions,
    private val saveSuggestion: SaveSuggestion,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: SearchState = SearchState()
) : BaseViewModel<SearchIntent, SearchState, SearchSignal>(initialState) {

    init {
        merge(intentsChannel.asFlow().processIntents(), connectivityReactionFlow)
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }

    private val connectivityReactionFlow: Flow<SearchState>
        get() = connectivityStateProvider.isConnectedFlow
            .withLatestFrom(states) { isConnected, currentState -> isConnected to currentState }
            .filter { (isConnected, currentState) ->
                isConnected && currentState.events.loadingFailed && currentState.events.data.isEmpty()
            }.map { (_, currentState) ->
                val resource = withContext(ioDispatcher) { searchEvents(currentState.searchText) }
                currentState.reduce(resource)
            }

    private fun Flow<SearchIntent>.processIntents(): Flow<SearchState> = merge(
        filterIsInstance<NewSearch>()
            .withLatestState()
            .processNewSearchIntents(),
        filterIsInstance<LoadMoreResults>()
            .withLatestState()
            .processLoadMoreResultsIntents(),
        filterIsInstance<ClearSelectionClicked>()
            .withLatestState()
            .processClearSelectionIntents(),
        filterIsInstance<EventLongClicked>()
            .withLatestState()
            .processEventLongClickedIntents(),
        filterIsInstance<HideSnackbarIntent>()
            .withLatestState()
            .processHideSnackbarIntents(),
        filterIsInstance<AddToFavouritesClicked>()
            .withLatestState()
            .processAddToFavouritesIntentsWithSnackbar(
                saveEvents = saveEvents,
                ioDispatcher = ioDispatcher,
                onDismissed = { viewModelScope.launch { send(HideSnackbar) } },
                sideEffect = { liveSignals.value = SearchSignal.FavouritesSaved }
            )
    )

    private fun Flow<Pair<NewSearch, SearchState>>.processNewSearchIntents(): Flow<SearchState> {
        return distinctUntilChangedBy { (intent, _) -> intent }
            .onEach { (intent, _) ->
                val (text, shouldSave) = intent
                if (shouldSave) saveSuggestion(text)
            }
            .flatMapLatest { (intent, currentState) ->
                val (text, _) = intent
                flow {
                    emit(currentState.copy(events = currentState.events.copyWithLoadingStatus))
                    val resource = viewModelScope.async {
                        withContext(ioDispatcher) { searchEvents(text) }
                    }
                    val suggestions = viewModelScope.async {
                        withContext(ioDispatcher) { getSearchSuggestions(text) }
                    }
                    emit(
                        currentState.reduce(
                            resource = resource.await(),
                            suggestions = suggestions.await(),
                            text = text
                        )
                    )
                }
            }
    }

    private fun Flow<Pair<LoadMoreResults, SearchState>>.processLoadMoreResultsIntents(): Flow<SearchState> {
        return filterNot { (_, currentState) ->
            val events = currentState.events
            events.status is Loading //TODO: maybe get rid of that condition and flatMapFirst?
                    || !events.canLoadMore
                    || events.data.isEmpty()
        }.flatMapLatest { (_, startState) -> //TODO: latest vs first vs concat?
            flow {
                var offset = startState.events.offset
                var resource: Resource<PagedResult<IEvent>>
                do {
                    resource = withContext(ioDispatcher) {
                        searchEvents(searchText = startState.searchText, offset = offset)
                    }
                    val newState = startState.reduce(resource)
                    ++offset
                } while (newState.events.status is LoadedSuccessfully
                    && newState.events.data.size == startState.events.data.size
                )
                emit(resource)
            }.withLatestState()
                .map { (resource, currentState) -> currentState.reduce(resource) }
                .onStart { emit(startState.copy(events = startState.events.copyWithLoadingStatus)) }
        }
    }
}
