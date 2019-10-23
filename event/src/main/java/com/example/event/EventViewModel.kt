package com.example.event

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.IsEventSaved
import com.example.coreandroid.util.Data
import com.example.coreandroid.util.LoadedSuccessfully
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EventViewModel(
    initialState: EventState,
    private val isEventSaved: IsEventSaved
) : VectorViewModel<EventState>(initialState) {
    init {
        viewModelScope.launch {
            isEventSaved(initialState.eventArg.id).collect {
                setState { copy(isFavourite = Data(status = LoadedSuccessfully, value = it)) }
            }
        }
    }
}