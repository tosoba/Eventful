package com.eventful.alarms

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
abstract class AlarmsViewModel
constructor(processor: AlarmsFlowProcessor, savedStateHandle: SavedStateHandle) :
    FlowViewModel<AlarmsIntent, AlarmsStateUpdate, AlarmsState, AlarmsSignal>(
        initialState = AlarmsState(savedStateHandle),
        processor = processor,
        savedStateHandle = savedStateHandle)
