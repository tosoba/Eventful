package com.eventful.event

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.provider.CurrentEventProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

@FlowPreview
@ExperimentalCoroutinesApi
class EventViewModel
@AssistedInject
constructor(@Assisted savedStateHandle: SavedStateHandle, processor: EventFlowProcessor) :
    FlowViewModel<EventIntent, EventStateUpdate, EventState, Unit>(
        EventState(savedStateHandle.get<Event>(EventArgs.EVENT.name)!!), processor),
    CurrentEventProvider {

    override val event: Flow<Event>
        get() = states.map { it.events.lastOrNull() }.filterNotNull().distinctUntilChanged()

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<EventViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): EventViewModel
    }
}
