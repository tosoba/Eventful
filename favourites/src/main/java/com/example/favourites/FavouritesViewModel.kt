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
        intentsChannel.asFlow()
            .onStart { emit(LoadFavourites) }
            .onEach { intent ->
                if (intent is RemoveFromFavouritesClicked) {
                    withContext(ioDispatcher) {
                        deleteEvents(state.events.data.filter { it.selected }.map { it.item })
                    }
                    liveEvents.value = FavouritesSignal.FavouritesRemoved
                }
            }
            .processIntents()
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }

    private fun Flow<FavouritesIntent>.processIntents(): Flow<FavouritesState> = merge(
        filterIsInstance<LoadFavourites>().processLoadFavouritesIntents(),
        filterIsInstance<EventLongClicked>().processEventLongClickedIntents { state },
        filterIsInstance<ClearSelectionClicked>().processClearSelectionIntents { state }
    )

    private fun Flow<LoadFavourites>.processLoadFavouritesIntents(): Flow<FavouritesState> {
        return filterNot {
            val state = statesChannel.value
            state.limitHit || state.events.status is Loading
        }.flatMapLatest {
            val outerState = statesChannel.value
            flowOf(outerState.copy(events = outerState.events.copyWithLoadingInProgress))
                .onCompletion {
                    emitAll(getSavedEvents(statesChannel.value.limit + limitIncrement)
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
