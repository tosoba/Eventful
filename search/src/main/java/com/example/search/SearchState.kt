package com.example.search

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.PagedDataList
import com.example.coreandroid.util.SelectableEventsSnackbarState
import com.haroldadmin.cnradapter.NetworkResponse
import java.util.*

data class SearchState(
    val searchText: String = "",
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    override val events: PagedDataList<Selectable<Event>> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableEventsSnackbarState<SearchState> {

    override fun copyWithTransformedEvents(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): SearchState = copy(events = events.transformItems(transform))

    override fun copyWithSnackbarStateAndTransformedEvents(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): SearchState = copy(
        events = events.transformItems(transform),
        snackbarState = snackbarState
    )

    override fun copyWithSnackbarState(snackbarState: SnackbarState): SearchState = copy(
        snackbarState = snackbarState
    )
}

internal fun SearchState.reduce(
    resource: Resource<PagedResult<IEvent>>,
    suggestions: List<SearchSuggestion>? = null,
    text: String? = null
): SearchState = when (resource) {
    is Resource.Success -> copy(
        events = events.copyWithNewItemsDistinct(
            resource.data.items.map { Selectable(Event(it)) },
            resource.data.currentPage + 1,
            resource.data.totalPages
        ) { (event, _) ->
            event.name.toLowerCase(Locale.getDefault()).trim()
        },
        searchSuggestions = suggestions ?: searchSuggestions,
        searchText = text ?: searchText
    )

    is Resource.Error<PagedResult<IEvent>> -> copy(
        events = events.copyWithFailureStatus(resource.error),
        snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
            if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504) {
                SnackbarState.Shown("No connection")
            } else {
                SnackbarState.Shown("Unknown network error.")
            }
        } else snackbarState,
        searchSuggestions = suggestions ?: searchSuggestions,
        searchText = text ?: searchText
    )
}
