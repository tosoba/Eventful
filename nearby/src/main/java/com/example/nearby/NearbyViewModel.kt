package com.example.nearby

import androidx.lifecycle.SavedStateHandle
import com.example.coreandroid.base.FlowViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class NearbyViewModel @AssistedInject constructor(
    processor: NearbyFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : FlowViewModel<NearbyIntent, NearbyStateUpdate, NearbyState, NearbySignal>(
    initialState = savedStateHandle["initialState"] ?: NearbyState(),
    processor = processor,
    savedStateHandle = savedStateHandle
) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<NearbyViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): NearbyViewModel
    }
}
