package com.example.favourites

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.LoadedSuccessfully
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class FavouritesViewModel(
    private val getSavedEvents: GetSavedEvents,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<FavouritesState>(FavouritesState.INITIAL) {

    private val limitIncrement: Int = 20

    private var getSavedEventsJob: Job? = null

    init {
//        getEvents()
        viewModelScope.launch {
            withContext(ioDispatcher) {
                getSavedEvents(limitIncrement)
                    .flowOn(Dispatchers.IO)
                    .onEach { Log.e("VM", it.toString()) }
                    .collect {
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

    fun loadMoreEvents() {
        getSavedEventsJob?.cancel()
//        getSavedEventsJob = getEvents()
    }

    override fun onCleared() {
        getSavedEventsJob?.cancel()
        super.onCleared()
    }

    private fun getEvents() {
        setState { copy(events = events.copyWithLoadingInProgress) }
        viewModelScope.launch {
            withState { state ->
                withContext(ioDispatcher) {

                    getSavedEvents(state.limit + limitIncrement)
                        .onEach { Log.e("VM", it.toString()) }
                        .collect {
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
    }
}