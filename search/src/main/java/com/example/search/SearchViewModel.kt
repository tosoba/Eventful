package com.example.search

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.util.Loading
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class SearchViewModel(
    private val searchEvents: SearchEvents,
    private val getSearchSuggestions: GetSeachSuggestions,
    private val saveSuggestion: SaveSuggestion,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: SearchState = SearchState()
) : BaseViewModel<SearchIntent, SearchState, Unit>(initialState) {

    init {
        merge(intentsChannel.asFlow().processIntents(), connectivityReactionFlow)
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }

    private val connectivityReactionFlow: Flow<SearchState>
        get() = connectivityStateProvider.isConnectedFlow.filter {
            val state = statesChannel.value
            it && state.events.loadingFailed && state.events.value.isEmpty()
        }.map {
            val state = statesChannel.value
            val resource = withContext(ioDispatcher) { searchEvents(state.searchText) }
            statesChannel.value.reduce(resource)
        }

    private fun Flow<SearchIntent>.processIntents(): Flow<SearchState> = merge(
        filterIsInstance<NewSearch>().processNewSearchIntents(),
        filterIsInstance<LoadMoreResults>().processLoadMoreResultsIntents()
    )

    private fun Flow<NewSearch>.processNewSearchIntents(): Flow<SearchState> {
        return distinctUntilChanged()
            .onEach { (text, shouldSave) -> if (shouldSave) saveSuggestion(text) }
            .mapLatest { (text, _) ->
                val resource = viewModelScope.async {
                    withContext(ioDispatcher) { searchEvents(text) }
                }
                val suggestions = viewModelScope.async {
                    withContext(ioDispatcher) { getSearchSuggestions(text) }
                }
                statesChannel.value.reduce(resource.await(), suggestions.await())
            }
    }

    private fun Flow<LoadMoreResults>.processLoadMoreResultsIntents(): Flow<SearchState> {
        return filterNot {
            val state = statesChannel.value
            state.events.status is Loading || state.events.offset >= state.events.totalItems
        }.flatMapFirst {
            flowOf(searchEvents(statesChannel.value.searchText))
                .map { statesChannel.value.reduce(it) }
        }
    }
}
