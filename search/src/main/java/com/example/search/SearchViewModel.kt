package com.example.search

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.arch.BaseViewModel
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.PagedDataList
import com.example.coreandroid.util.SnackbarState
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed class SearchIntent
data class NewSearch(val text: String, val confirmed: Boolean) : SearchIntent()
object LoadMoreResults

private fun SearchState.reduce(
    resource: Resource<PagedResult<IEvent>>,
    suggestions: List<SearchSuggestion>? = null
): SearchState = when (resource) {
    is Resource.Success -> copy(
        events = PagedDataList(
            resource.data.items.map { Event(it) },
            LoadedSuccessfully,
            resource.data.currentPage + 1,
            resource.data.totalPages
        ),
        searchSuggestions = suggestions ?: searchSuggestions
    )
    is Resource.Error<PagedResult<IEvent>, *> -> copy(
        events = events.copyWithError(resource.error),
        snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
            if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504) {
                SnackbarState.Text("No connection")
            } else {
                SnackbarState.Text("Unknown network error.")
            }
        } else snackbarState,
        searchSuggestions = suggestions ?: searchSuggestions
    )
}

@ExperimentalCoroutinesApi
@FlowPreview
class SearchVM(
    private val searchEvents: SearchEvents,
    private val getSearchSuggestions: GetSeachSuggestions,
    private val saveSuggestion: SaveSuggestion,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: SearchState = SearchState.INITIAL
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

class SearchViewModel(
    private val searchEvents: SearchEvents,
    private val getSearchSuggestions: GetSeachSuggestions,
    private val saveSuggestion: SaveSuggestion,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: SearchState = SearchState.INITIAL
) : VectorViewModel<SearchState>(initialState) {

    private var searchJob: Job? = null

    fun search(searchText: String, retry: Boolean = false) = withState { state ->
        if (searchText == state.searchText && !retry) return@withState

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            setState {
                copy(
                    events = events.copyWithLoadingInProgress,
                    searchText = searchText
                )
            }
            when (val result = withContext(ioDispatcher) {
                searchEvents(searchText)
            }) {
                is Resource.Success -> setState {
                    copy(
                        events = PagedDataList(
                            result.data.items.map { Event(it) },
                            LoadedSuccessfully,
                            result.data.currentPage + 1,
                            result.data.totalPages
                        )
                    )
                }

                is Resource.Error<PagedResult<IEvent>, *> -> setState {
                    copy(
                        events = events.copyWithError(result.error),
                        snackbarState = if (result.error is NetworkResponse.ServerError<*>) {
                            if ((result.error as NetworkResponse.ServerError<*>).code in 503..504) {
                                SnackbarState.Text("No connection")
                            } else {
                                SnackbarState.Text("Unknown network error.")
                            }
                        } else state.snackbarState
                    )
                }
            }
        }
    }

    fun searchMore() = withState { state ->
        if (state.events.status is Loading || state.events.offset >= state.events.totalItems)
            return@withState

        viewModelScope.launch {
            setState { copy(events = events.copyWithLoadingInProgress) }
            when (val result = withContext(ioDispatcher) {
                searchEvents(state.searchText)
            }) {
                is Resource.Success -> setState {
                    copy(
                        events = events.copyWithNewItems(
                            result.data.items.map { Event(it) },
                            result.data.currentPage + 1,
                            result.data.totalPages
                        )
                    )
                }

                is Resource.Error<PagedResult<IEvent>, *> -> setState {
                    copy(events = events.copyWithError(result.error))
                }
            }
        }
    }

    private var suggestionsJob: Job? = null

    fun loadSearchSuggestions(searchText: String) {
        suggestionsJob?.cancel()
        suggestionsJob = viewModelScope.launch {
            setState {
                copy(searchSuggestions = getSearchSuggestions(searchText))
            }
        }
    }

    fun insertNewSuggestion(searchText: String) {
        if (searchText.isNotBlank() && searchText.length > 3) viewModelScope.launch {
            saveSuggestion(searchText)
        }
    }

    fun onNotConnected() = setState {
        copy(
            events = events.copyWithError(SearchError.NotConnected),
            snackbarState = SnackbarState.Text("No connection")
        )
    }

    override fun onCleared() {
        searchJob?.cancel()
        suggestionsJob?.cancel()
        super.onCleared()
    }
}