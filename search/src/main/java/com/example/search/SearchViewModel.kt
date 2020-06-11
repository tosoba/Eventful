package com.example.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.provider.ConnectedStateProvider
import com.example.core.usecase.*
import com.example.core.util.Loading
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.util.*
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
    private val connectedStateProvider: ConnectedStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<SearchIntent, SearchStateUpdate, SearchState, SearchSignal>(
    savedStateHandle["initialState"] ?: SearchState()
) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<SearchViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): SearchViewModel
    }

    init {
        start()
    }

    override val updates: Flow<SearchStateUpdate>
        get() = merge(intents.updates, connectedStateProvider.updates)

    private val Flow<SearchIntent>.updates: Flow<SearchStateUpdate>
        get() = merge(
            filterIsInstance<SearchIntent.NewSearch>().newSearchUpdates,
            filterIsInstance<SearchIntent.LoadMoreResults>().loadMoreResultsUpdates,
            filterIsInstance<SearchIntent.ClearSelectionClicked>()
                .map { SearchStateUpdate.ClearSelection },
            filterIsInstance<SearchIntent.EventLongClicked>()
                .map { SearchStateUpdate.ToggleEventSelection(it.event) },
            filterIsInstance<SearchIntent.HideSnackbar>()
                .map { SearchStateUpdate.HideSnackbar },
            filterIsInstance<SearchIntent.AddToFavouritesClicked>().addToFavouritesUpdates
        )

    private val ConnectedStateProvider.updates: Flow<SearchStateUpdate>
        get() = connectedStates.filter { connected ->
            state.run { connected && events.loadingFailed && events.data.isEmpty() }
        }.flatMapFirst {
            searchEventsUpdates(newSearch = true, startWithLoading = false)
        }

    private val Flow<SearchIntent.NewSearch>.newSearchUpdates: Flow<SearchStateUpdate>
        get() = onEach { (text, shouldSave) -> if (shouldSave) saveSearchSuggestion(text) }
            .distinctUntilChangedBy { it.text }
            .flatMapLatest { (text) ->
                flow<SearchStateUpdate> {
                    emit(SearchStateUpdate.Events.Loading(searchText = text))
                    val suggestions = withContext(ioDispatcher) { getSearchSuggestions(text) }
                    emit(SearchStateUpdate.Suggestions(suggestions))
                }.onCompletion {
                    emitAll(searchEventsUpdates(newSearch = true, startWithLoading = false))
                }
            }

    private val Flow<SearchIntent.LoadMoreResults>.loadMoreResultsUpdates: Flow<SearchStateUpdate>
        get() = filterNot {
            val events = state.events
            events.status is Loading || !events.canLoadMore || events.data.isEmpty()
        }.flatMapFirst {
            searchEventsUpdates(newSearch = false, startWithLoading = true)
        }

    private fun searchEventsUpdates(
        newSearch: Boolean,
        startWithLoading: Boolean
    ): Flow<SearchStateUpdate> = state.let { startState ->
        getPagedEventsFlow(
            currentEvents = startState.events,
            toEvent = { selectable -> selectable.item }
        ) { offset ->
            searchEvents(
                searchText = startState.searchText,
                offset = if (newSearch) null else offset
            )
        }.map<Resource<PagedResult<IEvent>>, SearchStateUpdate> {
            SearchStateUpdate.Events.Loaded(it, newSearch)
        }.run {
            if (startWithLoading) onStart { emit(SearchStateUpdate.Events.Loading()) }
            else this
        }
    }

    private val Flow<SearchIntent.AddToFavouritesClicked>.addToFavouritesUpdates: Flow<SearchStateUpdate>
        get() = map {
            val selectedEvents = state.events.data.filter { it.selected }.map { it.item }
            withContext(ioDispatcher) { saveEvents(selectedEvents) }
            signal(SearchSignal.FavouritesSaved)
            SearchStateUpdate.Events.AddedToFavourites(
                snackbarText = addedToFavouritesMessage(eventsCount = selectedEvents.size),
                onSnackbarDismissed = { viewModelScope.launch { intent(SearchIntent.HideSnackbar) } }
            )
        }
}
