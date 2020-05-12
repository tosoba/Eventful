package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEvents
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.processClearSelectionIntents
import com.example.coreandroid.util.processEventLongClickedIntents
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
            .processIntents()
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }

    private fun Flow<FavouritesIntent>.processIntents(): Flow<FavouritesState> = merge(
        filterIsInstance<LoadFavourites>().withLatestState().processLoadFavouritesIntents(),
        filterIsInstance<EventLongClicked>().withLatestState().processEventLongClickedIntents(),
        filterIsInstance<ClearSelectionClicked>().withLatestState()
            .processClearSelectionIntents(),
        filterIsInstance<RemoveFromFavouritesClicked>().withLatestState()
            .map { (_, state) ->
                withContext(ioDispatcher) {
                    deleteEvents(state.events.data.filter { it.selected }.map { it.item })
                }
                liveSignals.value = FavouritesSignal.FavouritesRemoved
                state
            }
    )

    private fun Flow<Pair<LoadFavourites, FavouritesState>>.processLoadFavouritesIntents(): Flow<FavouritesState> {
        return filterNot { (_, currentState) -> currentState.events.limitHit }
            .flatMapFirst { (_, currentState) ->
                getSavedEvents(currentState.limit + limitIncrement)
                    .flowOn(ioDispatcher)
                    .map { events ->
                        currentState.copy(
                            events = DataList(
                                data = events.map { Selectable(Event(it)) },
                                status = LoadedSuccessfully,
                                limitHit = currentState.events.data.size == events.size
                            ),
                            limit = events.size
                        )
                    }
            }
    }

    companion object {
        const val limitIncrement: Int = 20 //TODO: move to settings later?
    }
}
