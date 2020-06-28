package com.example.event

import androidx.lifecycle.SavedStateHandle
import com.example.coreandroid.base.FlowViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class EventViewModel @AssistedInject constructor(
    processor: EventFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : FlowViewModel<EventIntent, EventStateUpdate, EventState, EventSignal>(
    initialState = EventState(event = savedStateHandle[EventFragment.EVENT_ARG_KEY]!!),
    processor = processor,
    savedStateHandle = savedStateHandle
) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<EventViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): EventViewModel
    }
}
