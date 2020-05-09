package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesViewModel(
    private val getSavedEvents: GetSavedEvents,
    private val deleteEvents: DeleteEvents,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: FavouritesState = FavouritesState()
) : BaseViewModel<FavouritesIntent, FavouritesState, FavouritesSignal>(initialState) {

    init {
        intentsWithLatestStates
            .onStart { emit(LoadFavourites to initialState) }
            .onEach { (intent, state) ->
                if (intent is RemoveFromFavouritesClicked) {
                    withContext(ioDispatcher) {
                        deleteEvents(state.events.data.filter { it.selected }.map { it.item })
                    }
                    liveSignals.value = FavouritesSignal.FavouritesRemoved
                }
            }
            .filterNot { (intent, _) -> intent is RemoveFromFavouritesClicked }
            .processIntents()
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }

    private fun Flow<Pair<FavouritesIntent, FavouritesState>>.processIntents(): Flow<FavouritesState> {
        return flatMapConcat { (intent, state) ->
            when (intent) {
                is LoadFavourites -> flowOf(intent to state).processLoadFavouritesIntents()
                is EventLongClicked -> flowOf(intent).processEventLongClickedIntents { state }
                is ClearSelectionClicked -> flowOf(intent).processClearSelectionIntents { state }
                else -> throw IllegalArgumentException()
            }
        }
    }

    private fun Flow<Pair<LoadFavourites, FavouritesState>>.processLoadFavouritesIntents(): Flow<FavouritesState> {
        return filterNot { (_, state) ->
            state.limitHit || state.events.status is Loading
        }.flatMapLatest { (_, state) ->
            flowOf(state.copy(events = state.events.copyWithLoadingInProgress))
                .onCompletion {
                    emitAll(getSavedEvents(state.limit + limitIncrement)
                        .flowOn(ioDispatcher)
                        .map { events ->
                            state.run {
                                copy(
                                    events = DataList(
                                        data = events.map { Selectable(Event(it)) },
                                        status = LoadedSuccessfully
                                    ),
                                    limit = events.size,
                                    limitHit = this.events.data.size == events.size
                                )
                            }
                        }
                    )
                }
        }
    }

    companion object {
        const val limitIncrement: Int = 20 //TODO: move to settings later?
    }
}
