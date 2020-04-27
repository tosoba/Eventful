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
            connectionReactionFlow
        ).onEach(statesChannel::send).launchIn(viewModelScope)
    }

    private val connectionReactionFlow: Flow<MainState>
        get() = getConnection().map { state.copy(isConnected = it) }

    private fun Flow<MainIntent>.processIntents(): Flow<MainState> = merge(
        filterIsInstance<LoadLocation>().processLoadLocationIntents(),
        filterIsInstance<ReloadLocation>().processReloadLocationIntents(),
        filterIsInstance<PermissionDenied>().processPermissionDeniedIntents()
    )

    private val locationLoadingStatesFlow: Flow<MainState>
        get() = flow {
            state.run {
                emit(copy(locationState = locationState.copy(status = LocationStatus.Loading)))
                val result = getLocation()
                emit(reduce(result))
            }
        }

    //TODO: either disable swipe refresh when event list is empty or use filterNot status is Loading
    private fun Flow<ReloadLocation>.processReloadLocationIntents(): Flow<MainState> {
        return flatMapFirst {
            flowOf(state.run {
                copy(locationState = locationState.copy(status = LocationStatus.Initial))
            }).onCompletion {
                emitAll(locationLoadingFlow)
            }
        }
    }

    private fun Flow<LoadLocation>.processLoadLocationIntents(): Flow<MainState> = take(1)
        .flatMapConcat { locationLoadingFlow }

    private val locationLoadingFlow: Flow<MainState>
        get() = getLocationAvailability()
            .distinctUntilChanged()
            .takeWhile { state.locationState.status !is LocationStatus.Found }
            .flatMapConcat {
                if (it) locationLoadingStatesFlow
                else flowOf(state.run {
                    copy(locationState = locationState.copy(status = LocationStatus.Disabled))
                })
            }

    //TODO: test this
    private fun Flow<PermissionDenied>.processPermissionDeniedIntents(): Flow<MainState> = map {
        state.run { copy(locationState = locationState.copy(status = LocationStatus.PermissionDenied)) }
    }

    override val isConnectedFlow: Flow<Boolean>
        get() = statesChannel.asFlow().map { it.isConnected }

    override val locationStateFlow: Flow<LocationState>
        get() = statesChannel.asFlow().map { it.locationState }
}
