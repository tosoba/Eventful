package com.example.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.model.search.SearchSuggestion
import com.example.core.provider.ConnectedStateProvider
import com.example.core.usecase.*
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.Loading
import com.example.core.util.PagedDataList
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.coreandroid.util.*
import com.haroldadmin.cnradapter.NetworkResponse
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class SearchViewModel @AssistedInject constructor(
    private val searchEvents: SearchEvents,
    private val getPagedEventsFlow: GetPagedEventsFlow,
    private val saveEvents: SaveEvents,
    private val getSearchSuggestions: GetSearchSuggestions,
    private val saveSearchSuggestion: SaveSearchSuggestion,
    connectedStateProvider: ConnectedStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<SearchIntent, SearchState, SearchSignal>(
    savedStateHandle["initialState"] ?: SearchState()
) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<SearchViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): SearchViewModel
    }

    init {
        merge(intents.updates, connectedStateProvider.updates)
            .applyToState(initialState = savedStateHandle["initialState"] ?: SearchState())
    }

    private val Flow<SearchIntent>.updates: Flow<Update>
        get() = merge(
            filterIsInstance<SearchIntent.NewSearch>().newSearchUpdates,
            filterIsInstance<SearchIntent.LoadMoreResults>().loadMoreResultsUpdates,
            filterIsInstance<SearchIntent.ClearSelectionClicked>().map { Update.ClearSelection },
            filterIsInstance<SearchIntent.EventLongClicked>().map { Update.ToggleEventSelection(it.event) },
            filterIsInstance<SearchIntent.HideSnackbar>().map { Update.HideSnackbar },
            filterIsInstance<SearchIntent.AddToFavouritesClicked>().addToFavouritesUpdates
        )

    private val ConnectedStateProvider.updates: Flow<Update>
        get() = connectedStates.filter { connected ->
            state.run { connected && events.loadingFailed && events.data.isEmpty() }
        }.flatMapFirst {
            searchEventsUpdates(newSearch = true, startWithLoading = false)
        }

    private val Flow<SearchIntent.NewSearch>.newSearchUpdates: Flow<Update>
        get() = onEach { (text, shouldSave) -> if (shouldSave) saveSearchSuggestion(text) }
            .distinctUntilChangedBy { it.text }
            .flatMapLatest { (text) ->
                flow<Update> {
                    emit(Update.Events.Loading(searchText = text))
                    val suggestions = withContext(ioDispatcher) { getSearchSuggestions(text) }
                    emit(Update.Suggestions(suggestions))
                }.onCompletion {
                    emitAll(searchEventsUpdates(newSearch = true, startWithLoading = false))
                }
            }

    private val Flow<SearchIntent.LoadMoreResults>.loadMoreResultsUpdates: Flow<Update>
        get() = filterNot {
            val events = state.events
            events.status is Loading || !events.canLoadMore || events.data.isEmpty()
        }.flatMapFirst {
            searchEventsUpdates(newSearch = false, startWithLoading = true)
        }

    private fun searchEventsUpdates(
        newSearch: Boolean,
        startWithLoading: Boolean
    ): Flow<Update> = state.let { startState ->
        getPagedEventsFlow(
            currentEvents = startState.events,
            toEvent = { selectable -> selectable.item }
        ) { offset ->
            searchEvents(
                searchText = startState.searchText,
                offset = if (newSearch) null else offset
            )
        }.map<Resource<PagedResult<IEvent>>, Update> {
            Update.Events.Loaded(it, newSearch)
        }.run {
            if (startWithLoading) onStart { emit(Update.Events.Loading()) }
            else this
        }
    }

    private val Flow<SearchIntent.AddToFavouritesClicked>.addToFavouritesUpdates: Flow<Update>
        get() = map {
            val selectedEvents = state.events.data.filter { it.selected }.map { it.item }
            withContext(ioDispatcher) { saveEvents(selectedEvents) }
            signal(SearchSignal.FavouritesSaved)
            Update.Events.AddedToFavourites(
                snackbarText = addedToFavouritesMessage(eventsCount = selectedEvents.size),
                onSnackbarDismissed = { viewModelScope.launch { intent(SearchIntent.HideSnackbar) } }
            )
        }

    private sealed class Update : StateUpdate<SearchState> {
        class ToggleEventSelection(
            override val event: Event
        ) : Update(),
            ToggleEventSelectionUpdate<SearchState>

        object ClearSelection : Update(), ClearSelectionUpdate<SearchState>

        object HideSnackbar : Update() {
            override fun invoke(state: SearchState): SearchState = state
                .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
        }

        class Suggestions(private val suggestions: List<SearchSuggestion>) : Update() {
            override fun invoke(state: SearchState): SearchState = state
                .copy(searchSuggestions = suggestions)
        }

        sealed class Events : Update() {
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
                                data = resource.data.items.map { Selectable(Event(it)) },
                                status = LoadedSuccessfully,
                                offset = resource.data.currentPage + 1,
                                limit = resource.data.totalPages
                            ) else events.copyWithNewItems(
                                newItems = resource.data.items.map { Selectable(Event(it)) },
                                offset = resource.data.currentPage + 1,
                                limit = resource.data.totalPages
                            )
                        )

                        is Resource.Error<PagedResult<IEvent>> -> copy(
                            events = events.copyWithFailureStatus(resource.error),
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

            class AddedToFavourites(
                override val snackbarText: String,
                override val onSnackbarDismissed: () -> Unit
            ) : Update(),
                EventSelectionConfirmedUpdate<SearchState>
        }
    }
}
