package com.example.search

import android.database.MatrixCursor
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.PagedDataList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class SearchUpdate
data class UpdateEvents(val events: PagedDataList<Selectable<Event>>) : SearchUpdate()
data class UpdateSnackbar(val state: SnackbarState) : SearchUpdate()
data class UpdateActionMode(val numberOfSelectedEvents: Int) : SearchUpdate()
data class SwapCursor(val cursor: MatrixCursor) : SearchUpdate()

@ExperimentalCoroutinesApi
@FlowPreview
internal fun SearchViewModel.updates(): Flow<SearchUpdate> = merge(
    states.map { it.events }
        .distinctUntilChanged()
        .map { UpdateEvents(it) },
    states.map { it.snackbarState }
        .distinctUntilChanged()
        .map { UpdateSnackbar(it) },
    states.map { state -> state.events.data.count { it.selected } }
        .distinctUntilChanged()
        .map { UpdateActionMode(it) },
    states.map { it.searchSuggestions to it.searchText }
        .filter { (suggestions, _) -> suggestions.isNotEmpty() }
        .distinctUntilChanged()
        .map { (suggestions, searchText) ->
            SwapCursor(
                MatrixCursor(SearchSuggestionsAdapter.COLUMN_NAMES)
                    .apply {
                        suggestions.filter { searchText != it.searchText }
                            .distinctBy { it.searchText }
                            .forEach { addRow(arrayOf(it.id, it.searchText, it.timestampMs)) }
                    }
            )
        }
)
