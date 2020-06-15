package com.example.search

import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.model.search.SearchSuggestion
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.PagedDataList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.util.ClearSelectionUpdate
import com.example.coreandroid.util.EventSelectionConfirmedUpdate
import com.example.coreandroid.util.StateUpdate
import com.example.coreandroid.util.ToggleEventSelectionUpdate
import com.haroldadmin.cnradapter.NetworkResponse

sealed class SearchStateUpdate :
    StateUpdate<SearchState> {
    class ToggleEventSelection(
        override val event: Event
    ) : SearchStateUpdate(),
        ToggleEventSelectionUpdate<SearchState>

    object ClearSelection :
        SearchStateUpdate(),
        ClearSelectionUpdate<SearchState>

    object HideSnackbar : SearchStateUpdate() {
        override fun invoke(state: SearchState): SearchState = state
            .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
    }

    class Suggestions(private val suggestions: List<SearchSuggestion>) : SearchStateUpdate() {
        override fun invoke(state: SearchState): SearchState = state
            .copy(searchSuggestions = suggestions)
    }

    sealed class Events : SearchStateUpdate() {
        class Loading(private val searchText: String? = null) : Events() {
            override fun invoke(state: SearchState): SearchState = state.copy(
                events = state.events.copyWithLoadingStatus,
                searchText = searchText ?: state.searchText
            )
        }

        class Loaded(
            private val resource: Resource<PagedResult<IEvent>>,
            private val newSearch: Boolean
        ) : Events() {
            override fun invoke(state: SearchState): SearchState = state.run {
                when (resource) {
                    is Resource.Success -> copy(
                        events = if (newSearch) PagedDataList(
                            data = resource.data.items.map {
                                Selectable(
                                    Event(it)
                                )
                            },
                            status = LoadedSuccessfully,
                            offset = resource.data.currentPage + 1,
                            limit = resource.data.totalPages
                        ) else events.copyWithNewItems(
                            newItems = resource.data.items.map {
                                Selectable(
                                    Event(it)
                                )
                            },
                            offset = resource.data.currentPage + 1,
                            limit = resource.data.totalPages
                        )
                    )

                    is Resource.Error<PagedResult<IEvent>> -> copy(
                        events = events.copyWithFailureStatus(resource.error),
                        snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
                            if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504) {
                                SnackbarState.Shown(
                                    "No connection"
                                )
                            } else {
                                SnackbarState.Shown(
                                    "Unknown network error."
                                )
                            }
                        } else snackbarState
                    )
                }
            }
        }

        class AddedToFavourites(
            override val snackbarText: String,
            override val onSnackbarDismissed: () -> Unit
        ) : SearchStateUpdate(),
            EventSelectionConfirmedUpdate<SearchState>
    }
}
