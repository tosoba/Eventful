package com.eventful.event.alarms

import androidx.lifecycle.SavedStateHandle
import com.eventful.alarms.AlarmsFlowProcessor
import com.eventful.alarms.AlarmsViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class EventAlarmsViewModel @AssistedInject constructor(
    @EventAlarmsViewModelProcessor processor: AlarmsFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : AlarmsViewModel(processor = processor, savedStateHandle = savedStateHandle) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<EventAlarmsViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): EventAlarmsViewModel
    }
}
