package com.eventful.event.details

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class EventDetailsViewModel @AssistedInject constructor(
    processor: EventDetailsFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : FlowViewModel<EventDetailsIntent, EventDetailsStateUpdate, EventDetailsState, EventDetailsSignal>(
    initialState = EventDetailsState(event = savedStateHandle[EventDetailsFragment.EVENT_ARG_KEY]!!),
    processor = processor,
    savedStateHandle = savedStateHandle
) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<EventDetailsViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): EventDetailsViewModel
    }
}
