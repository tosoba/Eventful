package com.example.eventsnearby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.coreandroid.base.FlowViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.model.location.LocationState
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.coreandroid.provider.PopBackStackSignal
import com.example.coreandroid.provider.PopBackStackSignalProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel @AssistedInject constructor(
    processor: MainFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) :
    FlowViewModel<MainIntent, MainStateUpdate, MainState, MainSignal>(
        initialState = MainState(savedStateHandle),
        processor = processor,
        savedStateHandle = savedStateHandle
    ),
    ConnectedStateProvider,
    LocationStateProvider,
    PopBackStackSignalProvider {

    override val connectedStates: Flow<Boolean> get() = states.map { it.connected }

    override val locationStates: Flow<LocationState> get() = states.map { it.locationState }
    override fun reloadLocation() {
        viewModelScope.launch { intent(MainIntent.ReloadLocation) }
    }

    override val popBackStackSignals: Flow<PopBackStackSignal>
        get() = signals.filterIsInstance<MainSignal.PopMainBackStackSignal>()

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<MainViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): MainViewModel
    }
}
