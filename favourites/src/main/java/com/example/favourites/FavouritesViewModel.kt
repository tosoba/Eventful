package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.LoadedSuccessfully
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavouritesViewModel(
    private val getSavedEvents: GetSavedEvents,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<FavouritesState>(FavouritesState.INITIAL) {

    private val limitIncrement: Int = 20

    private var getSavedEventsJob: Job

    init {
        getSavedEventsJob = getEvents(limitIncrement)
    }

    fun loadMoreEvents() {
        getSavedEventsJob.cancel()
        withState { state -> getSavedEventsJob = getEvents(state.limit + limitIncrement) }
    }

    override fun onCleared() {
        getSavedEventsJob.cancel()
        super.onCleared()
    }

    private fun getEvents(limit: Int): Job {
        setState { copy(events = events.copyWithLoadingInProgress) }
        return viewModelScope.launch {
            withContext(ioDispatcher) {
                getSavedEvents(limit)
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