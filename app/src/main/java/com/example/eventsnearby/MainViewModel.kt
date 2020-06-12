package com.example.eventsnearby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationState
import com.example.core.provider.ConnectedStateProvider
import com.example.core.provider.LocationStateProvider
import com.example.coreandroid.base.FlowViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel @AssistedInject constructor(
    processor: MainFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) :
    FlowViewModel<MainIntent, MainStateUpdate, MainState, Unit>(
        initialState = savedStateHandle["initialState"] ?: MainState(),
        processor = processor,
        savedStateHandle = savedStateHandle
    ),
    ConnectedStateProvider,
    LocationStateProvider {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<MainViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): MainViewModel
    }

    override val connectedStates: Flow<Boolean> get() = states.map { it.connected }

    override val locationStates: Flow<LocationState> get() = states.map { it.locationState }
    override fun reloadLocation() {
        viewModelScope.launch { intent(MainIntent.ReloadLocation) }
    }
}
