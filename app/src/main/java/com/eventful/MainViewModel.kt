package com.eventful

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.eventful.core.android.base.FlowViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.model.location.LocationState
import com.eventful.core.android.provider.ConnectedStateProvider
import com.eventful.core.android.provider.LocationStateProvider
import com.eventful.core.android.provider.PopBackStackSignal
import com.eventful.core.android.provider.PopBackStackSignalProvider
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
