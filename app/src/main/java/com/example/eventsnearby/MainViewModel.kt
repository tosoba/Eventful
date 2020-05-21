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
import com.example.coreandroid.provider.ConnectivityStateProvider
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

    override val isConnectedFlow: Flow<Boolean> get() = states.map { it.isConnected }
    override val locationStateFlow: Flow<LocationState> get() = states.map { it.locationState }

    private val connectionReactionFlow: Flow<MainState>
        get() = getConnection().map { state.copy(isConnected = it) }

    override fun reloadLocation() = viewModelScope.launch { send(ReloadLocation) }.let { Unit }

    private fun Flow<MainIntent>.processIntents(): Flow<MainState> = merge(
        filterIsInstance<LoadLocation>().processLoadLocationIntents(),
        filterIsInstance<ReloadLocation>().withLatestState().processReloadLocationIntents(),
        filterIsInstance<PermissionDenied>().withLatestState().processPermissionDeniedIntents()
    )

    private val locationLoadingStatesFlow: Flow<MainState>
        get() = flow {
            emit(LocationResult.Loading)
            val result = getLocation()
            emit(result)
        }.withLatestState()
            .map { (result, currentState) -> currentState.reduce(result) }

    //TODO: either disable swipe refresh when event list is empty or use filterNot status is Loading
    private fun Flow<Pair<ReloadLocation, MainState>>.processReloadLocationIntents(): Flow<MainState> {
        return flatMapFirst { (_, currentState) ->
            flowOf(
                currentState.copy(
                    locationState = currentState.locationState.copy(
                        status = LocationStatus.Initial
                    )
                )
            ).onCompletion {
                emitAll(locationLoadingFlow)
            }
        }
    }

    private fun Flow<LoadLocation>.processLoadLocationIntents(): Flow<MainState> = take(1)
        .flatMapLatest { locationLoadingFlow }

    private val locationLoadingFlow: Flow<MainState>
        get() = getLocationAvailability()
            .distinctUntilChanged()
            .combine(states) { item, state -> item to state }
            .takeWhile { (_, currentState) -> currentState.locationState.status !is LocationStatus.Found }
            .flatMapLatest { (locationAvailable, currentState) ->
                if (locationAvailable) locationLoadingStatesFlow
                else flowOf(
                    currentState.copy(
                        locationState = currentState.locationState.copy(
                            status = LocationStatus.Disabled
                        )
                    )
                )
            }

    //TODO: test this
    private fun Flow<Pair<PermissionDenied, MainState>>.processPermissionDeniedIntents(): Flow<MainState> {
        return map { (_, currentState) ->
            currentState.copy(
                locationState = currentState.locationState.copy(
                    status = LocationStatus.PermissionDenied
                )
            )
        }
    }
}
