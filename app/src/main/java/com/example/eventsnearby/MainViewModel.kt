package com.example.eventsnearby

import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationResult
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetConnection
import com.example.core.usecase.GetLocation
import com.example.core.usecase.GetLocationAvailability
import com.example.core.util.flatMapFirst
import com.example.coreandroid.arch.BaseViewModel
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class MainIntent
object LoadLocation : MainIntent()
object PermissionDenied : MainIntent()

private fun MainState.reduce(result: LocationResult): MainState = copy(
    locationState = if (result is LocationResult.Found) locationState.copy(
        latLng = result.latLng,
        status = LocationStatus.Found
    ) else locationState.copy(
        status = when (result) {
            is LocationResult.Disabled -> LocationStatus.Disabled
            is LocationResult.Error -> LocationStatus.Error(result.throwable)
            else -> locationState.status
        }
    )
)

//TODO: maybe observe permissions as well?

@ExperimentalCoroutinesApi
@FlowPreview
class MainVM(
    private val getLocation: GetLocation,
    private val getLocationAvailability: GetLocationAvailability,
    private val getConnection: GetConnection,
    initialState: MainState = MainState.INITIAL
) : BaseViewModel<MainIntent, MainState, Unit>(initialState),
    ConnectivityStateProvider,
    LocationStateProvider {

    init {
        merge(
            intentsChannel.asFlow().processIntents(),
            locationAvailabilityReactionFlow,
            connectionReactionFlow
        ).onEach(statesChannel::send).launchIn(viewModelScope)
    }

    private val locationAvailabilityReactionFlow: Flow<MainState>
        get() = getLocationAvailability()
            .filter {
                val locationState = statesChannel.value.locationState
                it && locationState.latLng == null && locationState.status !is LocationStatus.Loading
            }
            .flatMapFirst { locationLoadingStates }

    private val connectionReactionFlow: Flow<MainState>
        get() = getConnection().map { statesChannel.value.copy(isConnected = it) }

    private fun Flow<MainIntent>.processIntents(): Flow<MainState> = merge(
        filterIsInstance<LoadLocation>().processLoadLocationIntents(),
        filterIsInstance<PermissionDenied>().processPermissionDeniedIntents()
    )

    private val locationLoadingStates: Flow<MainState>
        get() = flow {
            val state = statesChannel.value
            emit(state.copy(locationState = state.locationState.copy(status = LocationStatus.Loading)))
            val result = getLocation()
            emit(state.reduce(result))
        }

    private fun Flow<LoadLocation>.processLoadLocationIntents(): Flow<MainState> = filterNot {
        statesChannel.value.locationState.status is LocationStatus.Loading
    }.flatMapFirst {
        locationLoadingStates
    }

    private fun Flow<PermissionDenied>.processPermissionDeniedIntents(): Flow<MainState> = map {
        val state = statesChannel.value
        state.copy(locationState = state.locationState.copy(status = LocationStatus.PermissionDenied))
    }

    override val isConnectedFlow: Flow<Boolean>
        get() = statesChannel.asFlow().map { it.isConnected }
    override val isConnected: Boolean get() = statesChannel.value.isConnected

    override val locationStateFlow: Flow<LocationState>
        get() = statesChannel.asFlow().map { it.locationState }
    override val locationState: LocationState get() = statesChannel.value.locationState
}

class MainViewModel(
    private val getLocation: GetLocation
) : VectorViewModel<MainState>(MainState.INITIAL),
    ConnectivityStateProvider,
    LocationStateProvider {

    override val isConnectedFlow: Flow<Boolean> get() = state.map { it.isConnected }
    override val isConnected: Boolean get() = currentState.isConnected

    override val locationStateFlow: Flow<LocationState> get() = state.map { it.locationState }
    override val locationState: LocationState get() = currentState.locationState

    var connected: Boolean
        set(value) = setState { copy(isConnected = value) }
        get() = currentState.isConnected

    fun loadLocation() = withState {
        if (it.locationState.status is LocationStatus.Loading) return@withState
        viewModelScope.launch {
            val result = getLocation()
            setState {
                copy(
                    locationState = if (result is LocationResult.Found) locationState.copy(
                        latLng = result.latLng,
                        status = LocationStatus.Found
                    ) else locationState.copy(
                        status = when (result) {
                            is LocationResult.Disabled -> LocationStatus.Disabled
                            is LocationResult.Error -> LocationStatus.Error(result.throwable)
                            else -> locationState.status
                        }
                    )
                )
            }
        }
    }

    fun onPermissionDenied() = setState {
        copy(locationState = locationState.copy(status = LocationStatus.PermissionDenied))
    }
}