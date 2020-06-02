package com.example.search

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveEvents
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.*
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class SearchViewModel(
    private val searchEvents: SearchEvents,
    private val saveEvents: SaveEvents,
    private val getSearchSuggestions: GetSeachSuggestions,
    private val saveSuggestion: SaveSuggestion,
    connectedStateProvider: ConnectedStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: SearchState = SearchState()
) : BaseViewModel<SearchIntent, SearchState, SearchSignal>(initialState) {

    init {
        merge(intents.updates, connectedStateProvider.updates)
            .applyToState(initialState = initialState)
    }

    private val ConnectedStateProvider.updates: Flow<Update>
        get() = connectedStates.filter { connected ->
            state.run { connected && events.loadingFailed && events.data.isEmpty() }
        }.flatMapFirst {
            val startState = state
            pagedEventsFlow(
                currentEvents = startState.events,
                dispatcher = ioDispatcher,
                toEvent = { selectable -> selectable.item }
            ) {
                searchEvents(startState.searchText)
            }.onStart {
                Update.Events.Loading()
            }.map { Update.Events.Loaded(it, true) }
        }

    private val Flow<SearchIntent>.updates: Flow<Update>
        get() = merge(
            filterIsInstance<NewSearch>().newSearchUpdates,
            filterIsInstance<LoadMoreResults>().loadMoreResultsUpdates,
            filterIsInstance<ClearSelectionClicked>().map { Update.ClearSelection },
            filterIsInstance<EventLongClicked>().map { Update.ToggleEventSelection(it.event) },
            filterIsInstance<HideSnackbar>().map { Update.HideSnackbar },
            filterIsInstance<AddToFavouritesClicked>().addToFavouritesUpdates
        )

    private val Flow<NewSearch>.newSearchUpdates: Flow<Update>
        get() = distinctUntilChanged()
            .onEach { intent ->
                val (text, shouldSave) = intent
                if (shouldSave) saveSuggestion(text)
            }
            .flatMapLatest { (text) ->
                flow<Update> {
                    emit(Update.Events.Loading(searchText = text))

                    val suggestions = withContext(ioDispatcher) { getSearchSuggestions(text) }
                    emit(Update.Suggestions(suggestions))
                }.onCompletion {
                    val startState = state
                    pagedEventsFlow(
                        currentEvents = startState.events,
                        dispatcher = ioDispatcher,
                        toEvent = { selectable -> selectable.item }
                    ) {
                        searchEvents(startState.searchText)
                    }.map { Update.Events.Loaded(it, true) }
                }
            }

    private val Flow<LoadMoreResults>.loadMoreResultsUpdates: Flow<Update>
        get() = filterNot {
            val events = state.events
            events.status is Loading || !events.canLoadMore || events.data.isEmpty()
        }.flatMapLatest {
            val startState = state
            pagedEventsFlow(
                currentEvents = startState.events,
                dispatcher = ioDispatcher,
                toEvent = { selectable -> selectable.item }
            ) { offset ->
                searchEvents(startState.searchText, offset)
            }.onStart {
                Update.Events.Loading()
            }.map { Update.Events.Loaded(it, false) }
        }

    private val Flow<AddToFavouritesClicked>.addToFavouritesUpdates: Flow<Update>
        get() = map {
            val selectedEvents = state.events.data.filter { it.selected }.map { it.item }
            withContext(ioDispatcher) { saveEvents(selectedEvents) }
            signal(SearchSignal.FavouritesSaved)
            Update.Events.AddedToFavourites(
                snackbarText = addedToFavouritesMessage(eventsCount = selectedEvents.size),
                onSnackbarDismissed = { viewModelScope.launch { intent(HideSnackbar) } }
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
