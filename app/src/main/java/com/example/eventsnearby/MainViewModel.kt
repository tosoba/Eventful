package com.example.eventsnearby

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationResult
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetLocation
import com.example.core.usecase.IsConnectedFlow
import com.example.core.usecase.IsLocationAvailableFlow
import com.example.core.util.ext.flatMapFirst
import com.example.core.util.ext.takeWhileInclusive
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.coreandroid.util.StateUpdate
import com.example.coreandroid.util.ext.isLocationAvailable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel(
    private val getLocation: GetLocation,
    isConnectedFlow: IsConnectedFlow,
    private val isLocationAvailableFlow: IsLocationAvailableFlow,
    private val appContext: Context,
    initialState: MainState = MainState()
) : BaseViewModel<MainIntent, MainState, Unit>(initialState),
    ConnectedStateProvider,
    LocationStateProvider {

    init {
        merge(
            intents.updates,
            isConnectedFlow().map { Update.Connection(it) },
            locationAvailabilityUpdates
        ).applyToState(initialState = initialState)
    }

    override val connectedStates: Flow<Boolean> get() = states.map { it.connected }

    override val locationStates: Flow<LocationState> get() = states.map { it.locationState }
    override fun reloadLocation() = viewModelScope.launch { intent(ReloadLocation) }.let { Unit }

    private val Flow<MainIntent>.updates: Flow<Update>
        get() = merge(
            filterIsInstance<LoadLocation>().take(1).flatMapLatest { locationLoadingUpdates },
            filterIsInstance<ReloadLocation>().reloadLocationUpdates,
            filterIsInstance<PermissionDenied>().map { Update.Location.PermissionDenied }
        )

    private val Flow<ReloadLocation>.reloadLocationUpdates: Flow<Update>
        get() = filterNot { state.locationState.status is LocationStatus.Loading }
            .flatMapFirst { locationLoadingUpdates.onStart { emit(Update.Location.Reset) } }

    private val locationAvailabilityUpdates: Flow<Update>
        get() = states.map { it.locationState }
            .filter { (latLng, status) -> status is LocationStatus.Disabled && latLng == null }
            .flatMapLatest { isLocationAvailableFlow().takeWhileInclusive { !it } }
            .filter { it }
            .flatMapLatest { locationLoadingUpdates }

    private val locationLoadingUpdates: Flow<Update>
        get() = flow<Update> {
            if (appContext.isLocationAvailable) {
                emit(Update.Location.Result(LocationResult.Loading))
                emit(Update.Location.Result(getLocation()))
            } else {
                emit(Update.Location.Result(LocationResult.Disabled))
            }
        }

    private sealed class Update : StateUpdate<MainState> {
        class Connection(private val connected: Boolean) : Update() {
            override fun invoke(state: MainState): MainState = state.copy(connected = connected)
        }

        sealed class Location : Update() {
            object PermissionDenied : Update() {
                override fun invoke(state: MainState): MainState = state.copy(
                    locationState = state.locationState.copy(status = LocationStatus.PermissionDenied)
                )
            }

            object Reset : Update() {
                override fun invoke(state: MainState): MainState = state.copy(
                    locationState = state.locationState.copy(status = LocationStatus.Initial)
                )
            }

            class Result(private val result: LocationResult) : Location() {
                override fun invoke(state: MainState): MainState = state.copy(
                    locationState = when (result) {
                        is LocationResult.Found -> state.locationState.copy(
                            latLng = result.latLng,
                            status = LocationStatus.Found
                        )
                        else -> state.locationState.copy(
                            status = when (result) {
                                is LocationResult.Loading -> LocationStatus.Loading
                                is LocationResult.Disabled -> LocationStatus.Disabled
                                is LocationResult.Error -> LocationStatus.Error(result.throwable)
                                else -> state.locationState.status
                            }
                        )
                    }
                )
            }
        }
    }
}
