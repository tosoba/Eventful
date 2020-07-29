package com.eventful.search

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.model.search.SearchSuggestion
import com.eventful.core.util.PagedDataList
import com.eventful.core.android.base.SelectableItemsSnackbarState
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable

data class SearchState(
    val searchText: String = "",
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    override val items: PagedDataList<Selectable<Event>> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableItemsSnackbarState<SearchState, Event> {

    constructor(savedStateHandle: SavedStateHandle) : this(
        searchText = savedStateHandle[KEY_SEARCH_TEXT] ?: ""
    )

    override fun copyWithTransformedItems(
        transform: (Selectable<Event>) -> Selectable<Event>
    ): SearchState = copy(items = items.transformItems(transform))

    override fun copyWithSnackbarStateAndTransformedItems(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): SearchState = copy(
        items = items.transformItems(transform),
        snackbarState = snackbarState
    )

    override fun copyWithSnackbarState(snackbarState: SnackbarState): SearchState = copy(
        snackbarState = snackbarState
    )

    companion object {
        const val KEY_SEARCH_TEXT = "key_search_text"
    }
}
