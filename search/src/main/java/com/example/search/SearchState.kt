package com.example.search

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.PagedDataList
import com.haroldadmin.cnradapter.NetworkResponse

data class SearchState(
    val searchText: String = "",
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    val events: PagedDataList<Event> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
)

internal fun SearchState.reduce(
    resource: Resource<PagedResult<IEvent>>,
    suggestions: List<SearchSuggestion>? = null
): SearchState = when (resource) {
    is Resource.Success -> copy(
        events = PagedDataList(
            resource.data.items.map {
                Event(
                    it
                )
            },
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