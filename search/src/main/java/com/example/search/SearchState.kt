package com.example.search

import androidx.lifecycle.SavedStateHandle
import com.example.core.model.search.SearchSuggestion
import com.example.core.util.PagedDataList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.base.SelectableEventsSnackbarState

data class SearchState(
    val searchText: String = "",
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    override val events: PagedDataList<Selectable<Event>> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableEventsSnackbarState<SearchState> {

    constructor(savedStateHandle: SavedStateHandle) : this(
        searchText = savedStateHandle[KEY_SEARCH_TEXT] ?: ""
    )

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

    companion object {
        const val KEY_SEARCH_TEXT = "key_search_text"
    }
}
