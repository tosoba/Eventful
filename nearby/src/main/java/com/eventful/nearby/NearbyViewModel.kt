package com.eventful.nearby

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*

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
