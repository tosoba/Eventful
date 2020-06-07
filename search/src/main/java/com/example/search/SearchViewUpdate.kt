package com.example.search

import android.database.MatrixCursor
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.core.util.PagedDataList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

internal sealed class SearchViewUpdate {
    data class Events(val events: PagedDataList<Selectable<Event>>) : SearchViewUpdate()
    data class Snackbar(val state: SnackbarState) : SearchViewUpdate()
    data class ActionMode(val numberOfSelectedEvents: Int) : SearchViewUpdate()
    data class SwapCursor(val cursor: MatrixCursor) : SearchViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
internal val SearchViewModel.viewUpdates: Flow<SearchViewUpdate>
    get() = merge(
        states.map { it.events }
            .distinctUntilChanged()
            .map { SearchViewUpdate.Events(it) },
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { SearchViewUpdate.Snackbar(it) },
        states.map { state -> state.events.data.count { it.selected } }
            .distinctUntilChanged()
            .map { SearchViewUpdate.ActionMode(it) },
        states.map { it.searchSuggestions to it.searchText }
            .filter { (suggestions, _) -> suggestions.isNotEmpty() }
            .distinctUntilChanged()
            .map { (suggestions, searchText) ->
                SearchViewUpdate.SwapCursor(
                    MatrixCursor(SearchSuggestionsAdapter.COLUMN_NAMES)
                        .apply {
                            suggestions.filter { searchText != it.searchText }
                                .distinctBy { it.searchText }
                                .forEach { addRow(arrayOf(it.id, it.searchText, it.timestampMs)) }
                        }
                )
            }
    )
