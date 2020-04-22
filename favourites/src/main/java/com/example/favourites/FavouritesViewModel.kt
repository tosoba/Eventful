package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
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
                    val state = statesChannel.value
                    withContext(ioDispatcher) {
                        deleteEvents(state.events.value.filter { it.selected }.map { it.item })
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
        filterIsInstance<EventLongClicked>().processEventLongClickedIntents(),
        filterIsInstance<ClearSelectionClicked>().processClearSelectionIntents()
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
                            val state = statesChannel.value
                            state.copy(
                                events = DataList(
                                    value = events.map { Selectable(Event(it)) },
                                    status = LoadedSuccessfully
                                ),
                                limit = events.size,
                                limitHit = state.events.value.size == events.size
                            )
                        }
                    )
                }
        }
    }

    //TODO: think about refactoring this out of viewModel classes
    // (via some extension methods and interfaces) since basically the same logic is in NearbyViewModel
    private fun Flow<EventLongClicked>.processEventLongClickedIntents(): Flow<FavouritesState> {
        return map { (event) ->
            val state = statesChannel.value
            state.copy(
                events = state.events.copy(
                    value = state.events.value.map {
                        if (it.item.id == event.id) Selectable(event, !it.selected) else it
                    }
                )
            )
        }
    }

    //TODO: think about refactoring this out of viewModel classes
    // (via some extension methods and interfaces) since basically the same logic is in NearbyViewModel
    private fun Flow<ClearSelectionClicked>.processClearSelectionIntents(): Flow<FavouritesState> {
        return map {
            val state = statesChannel.value
            state.copy(
                events = state.events.copy(
                    value = state.events.value.map { it.copy(selected = false) }
                )
            )
        }
    }

    companion object {
        const val limitIncrement: Int = 20 //TODO: move to settings later?
    }
}
