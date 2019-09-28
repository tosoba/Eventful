package com.example.search

import com.example.core.model.search.SearchSuggestion
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.PagedDataList
import com.haroldadmin.vector.VectorState

data class SearchState(
    val searchText: String,
    val searchSuggestions: List<SearchSuggestion>,
    val events: PagedDataList<Event>
) : VectorState {
    companion object {
        val INITIAL = SearchState("", emptyList(), PagedDataList())
    }
}