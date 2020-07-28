package com.example.search

import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.model.search.SearchSuggestion
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.PagedDataList
import com.example.coreandroid.base.ClearSelectionUpdate
import com.example.coreandroid.base.ItemSelectionConfirmedUpdate
import com.example.coreandroid.base.StateUpdate
import com.example.coreandroid.base.ToggleItemSelectionUpdate
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.core.model.Selectable
import com.haroldadmin.cnradapter.NetworkResponse

sealed class SearchStateUpdate : StateUpdate<SearchState> {
    data class ToggleEventSelection(
        override val item: Event
    ) : SearchStateUpdate(),
        ToggleItemSelectionUpdate<SearchState, Event, String> {
        override fun Event.id(): String = id
    }

    object ClearSelection :
        SearchStateUpdate(),
        ClearSelectionUpdate<SearchState, Event>

    object HideSnackbar : SearchStateUpdate() {
        override fun invoke(state: SearchState): SearchState = state
            .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
    }

    data class Suggestions(val suggestions: List<SearchSuggestion>) : SearchStateUpdate() {
        override fun invoke(state: SearchState): SearchState = state
            .copy(searchSuggestions = suggestions)
    }

    sealed class Events : SearchStateUpdate() {
        data class Loading(val searchText: String? = null) : Events() {
            override fun invoke(state: SearchState): SearchState = state.copy(
                items = state.items.copyWithLoadingStatus,
                searchText = searchText ?: state.searchText
            )
        }

        data class Loaded(
            val resource: Resource<PagedResult<IEvent>>,
            val newSearch: Boolean
        ) : Events() {
            override fun invoke(state: SearchState): SearchState = state.run {
                when (resource) {
                    is Resource.Success -> copy(
                        items = if (newSearch) PagedDataList(
                            data = resource.data.items.map { Selectable(Event(it)) },
                            status = LoadedSuccessfully,
                            offset = resource.data.currentPage + 1,
                            limit = resource.data.totalPages
                        ) else items.copyWithNewItems(
                            newItems = resource.data.items.map { Selectable(Event(it)) },
                            offset = resource.data.currentPage + 1,
                            limit = resource.data.totalPages
                        )
                    )

                    is Resource.Error<PagedResult<IEvent>> -> copy(
                        items = items.copyWithFailureStatus(resource.error),
                        snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
                            if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504) {
                                SnackbarState.Shown("No connection")
                            } else {
                                SnackbarState.Shown("Unknown network error.")
                            }
                        } else snackbarState
                    )
                }
            }
        }

        data class AddedToFavourites(
            override val snackbarText: String,
            override val onSnackbarDismissed: () -> Unit
        ) : SearchStateUpdate(),
            ItemSelectionConfirmedUpdate<SearchState, Event>
    }
}
