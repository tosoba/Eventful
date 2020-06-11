package com.example.eventsnearby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationResult
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.provider.ConnectedStateProvider
import com.example.core.provider.LocationStateProvider
import com.example.core.usecase.GetLocation
import com.example.core.usecase.IsConnectedFlow
import com.example.core.usecase.IsLocationAvailableFlow
import com.example.core.util.ext.flatMapFirst
import com.example.core.util.ext.takeWhileInclusive
import com.example.coreandroid.base.BaseViewModel
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
    private val getLocation: GetLocation,
    private val isConnectedFlow: IsConnectedFlow,
    private val isLocationAvailableFlow: IsLocationAvailableFlow,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<MainIntent, MainStateUpdate, MainState, Unit>(
    initialState = savedStateHandle["initialState"] ?: MainState()
), ConnectedStateProvider, LocationStateProvider {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<MainViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): MainViewModel
    }

    init {
        start()
    }

    override val updates: Flow<MainStateUpdate>
        get() = merge(
            intents.updates,
            isConnectedFlow().distinctUntilChanged().map { MainStateUpdate.Connection(it) },
            locationAvailabilityUpdates
        )

    override val connectedStates: Flow<Boolean> get() = states.map { it.connected }

    override val locationStates: Flow<LocationState> get() = states.map { it.locationState }
    override fun reloadLocation() {
        viewModelScope.launch { intent(MainIntent.ReloadLocation) }
    }

    private val Flow<MainIntent>.updates: Flow<MainStateUpdate>
        get() = merge(
            filterIsInstance<MainIntent.LoadLocation>()
                .take(1)
                .flatMapLatest { locationLoadingUpdates },
            filterIsInstance<MainIntent.ReloadLocation>().reloadLocationUpdates,
            filterIsInstance<MainIntent.PermissionDenied>().map { MainStateUpdate.Location.PermissionDenied }
        )

    private val Flow<MainIntent.ReloadLocation>.reloadLocationUpdates: Flow<MainStateUpdate>
        get() = filterNot { state.locationState.status is LocationStatus.Loading }
            .flatMapFirst { locationLoadingUpdates.onStart { emit(MainStateUpdate.Location.Reset) } }

    private val locationAvailabilityUpdates: Flow<MainStateUpdate>
        get() = states.map { it.locationState }
            .filter { (latLng, status) -> status is LocationStatus.Disabled && latLng == null }
            .flatMapLatest { isLocationAvailableFlow().takeWhileInclusive { !it } }
            .filter { it }
            .flatMapLatest { locationLoadingUpdates }

    private val locationLoadingUpdates: Flow<MainStateUpdate>
        get() = flow<MainStateUpdate> {
            emit(MainStateUpdate.Location.Result(LocationResult.Loading))
            emit(MainStateUpdate.Location.Result(getLocation()))
        }
}
