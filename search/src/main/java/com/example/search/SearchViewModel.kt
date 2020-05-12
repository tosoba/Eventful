package com.example.search

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveEvents
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.processClearSelectionIntents
import com.example.coreandroid.util.processEventLongClickedIntents
import com.example.coreandroid.util.withLatestFrom
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
            filterIsInstance<AddToFavouritesClicked>().withLatestState()
                .processAddToFavouritesIntents()
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
        return filterNot { (_, currentState) ->
            currentState.events.status is Loading || !currentState.events.canLoadMore
        }.flatMapFirst { (_, currentState) ->
            flow {
                emit(currentState.copy(events = currentState.events.copyWithLoadingStatus))
                val resource = viewModelScope.async {
                    withContext(ioDispatcher) {
                        searchEvents(currentState.searchText, currentState.events.offset)
                    }
                }
                emit(currentState.reduce(resource = resource.await()))
            }
        }
    }

    private fun Flow<Pair<AddToFavouritesClicked, SearchState>>.processAddToFavouritesIntents(): Flow<SearchState> {
        return map { (_, currentState) ->
            withContext(ioDispatcher) {
                saveEvents(currentState.events.data.filter { it.selected }.map { it.item })
            }
            liveSignals.value = SearchSignal.FavouritesSaved
            currentState.copy(events = currentState.events.transformItems { it.copy(selected = false) })
        }
    }
}
