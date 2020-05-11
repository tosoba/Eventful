package com.example.search

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveEvents
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.processClearSelectionIntents
import com.example.coreandroid.util.processEventLongClickedIntents
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class SearchViewModel(
    private val searchEvents: SearchEvents,
    private val saveEvents: SaveEvents,
    private val getSearchSuggestions: GetSeachSuggestions,
    private val saveSuggestion: SaveSuggestion,
    private val connectivityStateProvider: ConnectivityStateProvider,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: SearchState = SearchState()
) : BaseViewModel<SearchIntent, SearchState, SearchSignal>(initialState) {

    init {
        merge(intentsChannel.asFlow().processIntents(), connectivityReactionFlow)
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }

    private val connectivityReactionFlow: Flow<SearchState>
        get() = connectivityStateProvider.isConnectedFlow.filter {
            val state = statesChannel.value
            it && state.events.loadingFailed && state.events.data.isEmpty()
        }.map {
            state.run {
                val resource = withContext(ioDispatcher) { searchEvents(searchText) }
                reduce(resource)
            }
        }

    private fun Flow<SearchIntent>.processIntents(): Flow<SearchState> = merge(
        filterIsInstance<NewSearch>().processNewSearchIntents(),
        filterIsInstance<LoadMoreResults>().processLoadMoreResultsIntents(),
        filterIsInstance<ClearSelectionClicked>().processClearSelectionIntents { state },
        filterIsInstance<EventLongClicked>().processEventLongClickedIntents { state },
        filterIsInstance<AddToFavouritesClicked>().processAddToFavouritesIntents()
    )

    private fun Flow<NewSearch>.processNewSearchIntents(): Flow<SearchState> {
        return distinctUntilChanged()
            .onEach { (text, shouldSave) -> if (shouldSave) saveSuggestion(text) }
            .flatMapLatest { (text, _) ->
                state.run {
                    flow {
                        emit(copy(events = events.copyWithLoadingStatus))
                        val resource = viewModelScope.async {
                            withContext(ioDispatcher) { searchEvents(text) }
                        }
                        val suggestions = viewModelScope.async {
                            withContext(ioDispatcher) { getSearchSuggestions(text) }
                        }
                        emit(
                            reduce(
                                resource = resource.await(),
                                suggestions = suggestions.await(),
                                text = text
                            )
                        )
                    }
                }
            }
    }

    private fun Flow<LoadMoreResults>.processLoadMoreResultsIntents(): Flow<SearchState> {
        return filterNot {
            state.run { events.status is Loading || events.offset >= events.limit }
        }.flatMapFirst {
            state.run {
                flow {
                    emit(copy(events = events.copyWithLoadingStatus))
                    val resource = viewModelScope.async {
                        withContext(ioDispatcher) { searchEvents(searchText, events.offset) }
                    }
                    emit(reduce(resource = resource.await()))
                }
            }
        }
    }

    private fun Flow<AddToFavouritesClicked>.processAddToFavouritesIntents(): Flow<SearchState> {
        return map {
            state.run {
                withContext(ioDispatcher) {
                    saveEvents(events.data.filter { it.selected }.map { it.item })
                }
                liveSignals.value = SearchSignal.FavouritesSaved
                copy(events = events.transformItems { it.copy(selected = false) })
            }
        }
    }
}
