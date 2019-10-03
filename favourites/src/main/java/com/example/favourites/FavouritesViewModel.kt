package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.ticketmaster.Event
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavouritesViewModel(
    private val getSavedEvents: GetSavedEvents
) : VectorViewModel<FavouritesState>(FavouritesState.INITIAL) {

    private val limitIncrement: Int = 20

    private var getSavedEventsJob: Job = getEvents()

    fun loadMoreEvents() {
        getSavedEventsJob.cancel()
        getSavedEventsJob = getEvents()
    }

    override fun onCleared() {
        getSavedEventsJob.cancel()
        super.onCleared()
    }

    private fun getEvents(): Job = viewModelScope.launch {
        withState { state ->
            getSavedEvents(state.limit + limitIncrement).collect {
                setState {
                    copy(
                        events = events.copyWithNewItems(it.map { Event(it) }),
                        limit = limit + limitIncrement
                    )
                }
            }
        }
    }
}