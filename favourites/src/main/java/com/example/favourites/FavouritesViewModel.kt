package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.arch.BaseViewModel
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class FavouritesIntent
object LoadFavourites : FavouritesIntent()

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesVM(
    private val getSavedEvents: GetSavedEvents,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: FavouritesState = FavouritesState()
) : BaseViewModel<FavouritesIntent, FavouritesState, Unit>(initialState) {
    init {
        intentsChannel.asFlow()
            .onStart { emit(LoadFavourites) }
            .filterNot {
                val state = statesChannel.value
                state.limitHit || state.events.status is Loading
            }
            .flatMapLatest {
                val outerState = statesChannel.value
                flowOf(outerState.copy(events = outerState.events.copyWithLoadingInProgress))
                    .onCompletion {
                        emitAll(getSavedEvents(statesChannel.value.limit + limitIncrement)
                            .flowOn(ioDispatcher)
                            .map { events ->
                                val state = statesChannel.value
                                state.copy(
                                    events = DataList(
                                        value = events.map { Event(it) },
                                        status = LoadedSuccessfully
                                    ),
                                    limit = events.size,
                                    limitHit = state.events.value.size == events.size
                                )
                            }
                        )
                    }
            }
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }

    companion object {
        const val limitIncrement: Int = 20 //TODO: move to settings later?
    }
}
