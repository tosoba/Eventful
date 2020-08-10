package com.eventful.search

import android.database.MatrixCursor
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable
import com.eventful.core.util.PagedDataList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class SearchViewUpdate {
    data class Events(val events: PagedDataList<Selectable<Event>>) : SearchViewUpdate()
    data class Snackbar(val state: SnackbarState) : SearchViewUpdate()
    data class UpdateActionMode(val numberOfSelectedEvents: Int) : SearchViewUpdate()
    data class SwapCursor(val cursor: MatrixCursor) : SearchViewUpdate()
    object FinishActionMode : SearchViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val SearchViewModel.viewUpdates: Flow<SearchViewUpdate>
    get() = merge(
        states.map { it.items }
            .distinctUntilChanged()
            .map { SearchViewUpdate.Events(it) },
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { SearchViewUpdate.Snackbar(it) },
        states.map { state -> state.items.data.count { it.selected } }
            .distinctUntilChanged()
            .map { SearchViewUpdate.UpdateActionMode(it) },
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
            },
        signals.filterIsInstance<SearchSignal.FavouritesSaved>()
            .map { SearchViewUpdate.FinishActionMode }
    )
