package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.arch.BaseViewModel
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed class FavouritesIntent
object LoadFavourites : FavouritesIntent()

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesVM(
    private val getSavedEvents: GetSavedEvents,
    private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel<FavouritesIntent, FavouritesState, Unit>(FavouritesState.INITIAL) {
    init {
        intents.asFlow()
            .onStart { emit(LoadFavourites) }
            .map { Pair(it, _states.value) }
            .filterNot { (_, state) -> state.limitHit }
            .flatMapLatest { (_, state) ->
                flowOf(state.copy(events = state.events.copyWithLoadingInProgress))
                    .onCompletion {
                        emitAll(getSavedEvents(state.limit + limitIncrement)
                            .flowOn(ioDispatcher)
                            .map { events ->
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
            .onEach(_states::send)
            .launchIn(viewModelScope)
    }

    companion object {
        const val limitIncrement: Int = 20 //TODO: move to settings later?
    }
}

class FavouritesViewModel(
    private val getSavedEvents: GetSavedEvents,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<FavouritesState>(FavouritesState.INITIAL) {

    private var getSavedEventsJob: Job = getEvents(limitIncrement)

    fun loadMoreEvents() = withState { (events, limit) ->
        if (events.status is Loading) return@withState
        getSavedEventsJob.cancel()
        getSavedEventsJob = getEvents(limit + limitIncrement)
    }

    override fun onCleared() {
        getSavedEventsJob.cancel()
        super.onCleared()
    }

    private fun getEvents(limit: Int): Job {
        setState { copy(events = events.copyWithLoadingInProgress) }
        return viewModelScope.launch {
            withContext(ioDispatcher) {
                getSavedEvents(limit).collect {
                    setState {
                        copy(
                            events = DataList(
                                value = it.map { Event(it) },
                                status = LoadedSuccessfully
                            ),
                            limit = it.size
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val limitIncrement: Int = 20
    }
}