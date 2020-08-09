package com.eventful.all.alarms

import androidx.lifecycle.SavedStateHandle
import com.eventful.alarms.AlarmsFlowProcessor
import com.eventful.alarms.AlarmsViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Named

@ExperimentalCoroutinesApi
@FlowPreview
class AllAlarmsViewModel @AssistedInject constructor(
    @Named("AllAlarmsViewModelProcessor") processor: AlarmsFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : AlarmsViewModel(processor = processor, savedStateHandle = savedStateHandle) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<AllAlarmsViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): AllAlarmsViewModel
    }
}
