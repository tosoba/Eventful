package com.example.alarms

import androidx.lifecycle.SavedStateHandle
import com.example.coreandroid.base.FlowViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class AlarmsViewModel @AssistedInject constructor(
    processor: AlarmsFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : FlowViewModel<AlarmsIntent, AlarmsStateUpdate, AlarmsState, AlarmsSignal>(
    initialState = AlarmsState(event = savedStateHandle[AlarmsFragment.EVENT_ARG_KEY]),
    processor = processor,
    savedStateHandle = savedStateHandle
) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<AlarmsViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): AlarmsViewModel
    }
}
