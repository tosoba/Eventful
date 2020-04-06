package com.example.search

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.PagedDataList
import com.example.coreandroid.util.SnackbarState
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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