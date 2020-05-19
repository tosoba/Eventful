package com.example.search

import androidx.lifecycle.viewModelScope
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

    private fun Flow<SearchIntent>.processIntents(): Flow<SearchState> {
        return merge(
            filterIsInstance<NewSearch>().withLatestState().processNewSearchIntents(),
            filterIsInstance<LoadMoreResults>().withLatestState().processLoadMoreResultsIntents(),
            filterIsInstance<ClearSelectionClicked>().withLatestState()
                .processClearSelectionIntents(),
            filterIsInstance<EventLongClicked>().withLatestState().processEventLongClickedIntents(),
            filterIsInstance<HideSnackbarIntent>().withLatestState().processHideSnackbarIntents(),
            filterIsInstance<AddToFavouritesClicked>().withLatestState()
                .processAddToFavouritesIntentsWithSnackbar(
                    saveEvents = saveEvents,
                    ioDispatcher = ioDispatcher,
                    onDismissed = { viewModelScope.launch { send(HideSnackbar) } },
                    sideEffect = { liveSignals.value = SearchSignal.FavouritesSaved }
                )
        )
    }

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
        return filterNot { (intent, currentState) ->
            val events = currentState.events
            (events.status is Loading && intent.offset == null) //TODO: maybe get rid of that condition and flatMapFirst?
                    || !events.canLoadMore
                    || events.data.isEmpty()
        }.flatMapLatest { (intent, startState) -> //TODO: latest vs first vs concat?
            val resourceFlow = flowOf(
                searchEvents(
                    searchText = startState.searchText,
                    offset = intent.offset ?: startState.events.offset
                )
            )
            resourceFlow
                .flowOn(ioDispatcher)
                .withLatestState()
                .map { (resource, state) -> state.reduce(resource) }
                .onStart { emit(startState.copy(events = startState.events.copyWithLoadingStatus)) }
                .onEach { newState ->
                    if (newState.events.data.size == startState.events.data.size
                        && newState.events.status is LoadedSuccessfully
                    ) {
                        send(LoadMoreResults(newState.events.offset)) //TODO: maybe instead of sending another intent replace it with a while loop or smth
                    }
                }
        }
    }
}
