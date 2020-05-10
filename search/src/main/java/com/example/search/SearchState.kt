package com.example.search

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.PagedDataList
import com.example.coreandroid.util.SelectableEventsState
import com.haroldadmin.cnradapter.NetworkResponse

data class SearchState(
    val searchText: String = "",
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    override val events: PagedDataList<Selectable<Event>> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableEventsState<SearchState> {
    override fun copyWithTransformedEvents(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): SearchState = copy(events = events.transformItems(transform))
}

internal fun SearchState.reduce(
    resource: Resource<PagedResult<IEvent>>,
    suggestions: List<SearchSuggestion>? = null
): SearchState = when (resource) {
    is Resource.Success -> copy(
        events = events.copyWithNewItems(
            resource.data.items.map { Selectable(Event(it)) },
            resource.data.currentPage + 1,
            resource.data.totalPages
        ),
        searchSuggestions = suggestions ?: searchSuggestions
    )

    is Resource.Error<PagedResult<IEvent>, *> -> copy(
        events = events.copyWithFailureStatus(resource.error),
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
