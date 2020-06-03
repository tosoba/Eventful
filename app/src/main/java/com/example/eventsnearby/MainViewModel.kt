package com.example.eventsnearby

import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationResult
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetConnection
import com.example.core.usecase.GetLocation
import com.example.core.usecase.GetLocationAvailability
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.util.StateUpdate
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

//TODO: maybe observe permissions as well?

@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel(
    private val getLocation: GetLocation,
    private val getLocationAvailability: GetLocationAvailability,
    getConnection: GetConnection,
    initialState: MainState = MainState()
) : BaseViewModel<MainIntent, MainState, Unit>(initialState),
    ConnectedStateProvider,
    LocationStateProvider {

    init {
        merge(intents.updates, getConnection().map { Update.Connection(it) })
            .applyToState(initialState = initialState)
    }

    override val connectedStates: Flow<Boolean> get() = states.map { it.connected }

    override val locationStates: Flow<LocationState> get() = states.map { it.locationState }
    override fun reloadLocation() = viewModelScope.launch { intent(ReloadLocation) }.let { Unit }

    private val Flow<MainIntent>.updates: Flow<Update>
        get() = merge(
            filterIsInstance<LoadLocation>().loadLocationUpdates,
            filterIsInstance<ReloadLocation>().reloadLocationUpdates,
            filterIsInstance<PermissionDenied>().permissionDeniedUpdates
        )

    private val Flow<LoadLocation>.loadLocationUpdates: Flow<Update>
        get() = take(1).flatMapLatest { locationLoadingUpdates }

    private val Flow<ReloadLocation>.reloadLocationUpdates: Flow<Update>
        get() = filterNot { state.locationState.status is LocationStatus.Loading }
            .flatMapFirst {
                flowOf<Update>(Update.Location.Reset)
                    .onCompletion { emitAll(locationLoadingUpdates) }
            }

    private val locationLoadingUpdates: Flow<Update>
        get() = getLocationAvailability()
            .distinctUntilChanged()
            .takeWhile { state.locationState.status !is LocationStatus.Found }
            .flatMapLatest { locationAvailable ->
                flow<Update> {
                    if (locationAvailable) {
                        emit(Update.Location.Result(LocationResult.Loading))
                        emit(Update.Location.Result(getLocation()))
                    } else {
                        emit(Update.Location.Result(LocationResult.Disabled))
                    }
                }
            }

    private val Flow<PermissionDenied>.permissionDeniedUpdates: Flow<Update>
        get() = map { Update.Location.PermissionDenied }

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
