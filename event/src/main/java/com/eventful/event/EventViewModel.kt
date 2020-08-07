package com.eventful.event

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class EventViewModel @AssistedInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    processor: EventFlowProcessor
) : FlowViewModel<EventIntent, EventStateUpdate, EventState, Unit>(
    EventState(savedStateHandle[EventFragment.EVENT_ARG_KEY]!!),
    processor
) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<EventViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): EventViewModel
    }
}
