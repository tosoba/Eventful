package com.example.eventsnearby

import androidx.lifecycle.viewModelScope
import com.example.core.model.app.LocationState
import com.example.core.model.app.LocationStatus
import com.example.core.usecase.GetConnection
import com.example.core.usecase.GetLocation
import com.example.core.usecase.GetLocationAvailability
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

//TODO: maybe observe permissions as well?

@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel(
    private val getLocation: GetLocation,
    private val getLocationAvailability: GetLocationAvailability,
    private val getConnection: GetConnection,
    initialState: MainState = MainState()
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

    override val locationStateFlow: Flow<LocationState>
        get() = statesChannel.asFlow().map { it.locationState }
}
