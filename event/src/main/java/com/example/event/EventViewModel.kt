package com.example.event

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSaved
import com.example.core.usecase.SaveEvent
import com.example.coreandroid.util.Data
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.LoadingFailed
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EventViewModel(
    initialState: EventState,
    private val isEventSaved: IsEventSaved,
    private val saveEvent: SaveEvent,
    private val deleteEvent: DeleteEvent
) : VectorViewModel<EventState>(initialState) {

    init {
        viewModelScope.launch {
            isEventSaved(initialState.eventArg.id)
                .map { Data(it, LoadedSuccessfully) }
                .catch { emit(Data(false, LoadingFailed(it))) }
                .collect {
                    setState {
                        if (it.status is LoadingFailed<*>)
                            copy(isFavourite = isFavourite.copyWithError((it.status as LoadingFailed<*>).error))
                        else copy(isFavourite = it)
                    }
                }
        }
    }

    fun toggleEventFavourite() = withState { state ->
        if (state.isFavourite.status is Loading) return@withState

        setState { copy(isFavourite = isFavourite.copyWithLoadingInProgress) }

        viewModelScope.launch {
            if (state.isFavourite.data) deleteEvent(state.eventArg)
            else saveEvent(state.eventArg)
        }
    }
}