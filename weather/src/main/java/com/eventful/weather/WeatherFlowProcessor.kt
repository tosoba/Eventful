package com.eventful.weather

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowProcessor
import com.eventful.core.android.provider.CurrentEventProvider
import com.eventful.core.usecase.weather.GetForecast
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherFlowProcessor @Inject constructor(
    private val getForecast: GetForecast,
    private val currentEventProvider: CurrentEventProvider,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<WeatherIntent, WeatherStateUpdate, WeatherState, Unit> {

    override fun stateWillUpdate(
        currentState: WeatherState,
        nextState: WeatherState,
        update: WeatherStateUpdate,
        savedStateHandle: SavedStateHandle
    ) {
        if (update is WeatherStateUpdate.NewEvent) {
            savedStateHandle[WeatherArgs.EVENT.name] = update.event
        }
    }

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<WeatherIntent>,
        currentState: () -> WeatherState,
        states: Flow<WeatherState>,
        intent: suspend (WeatherIntent) -> Unit,
        signal: suspend (Unit) -> Unit
    ): Flow<WeatherStateUpdate> = merge(
        currentEventProvider.event
            .filter { WeatherEventValidator.isValid(it) }
            .map { WeatherStateUpdate.NewEvent(it) },
        intents.filterIsInstance<WeatherIntent.TabSelected>()
            .map { WeatherStateUpdate.TabSelected(it.tab) },
        states.map { (event, _, _) -> requireNotNull(event.venues?.firstOrNull()?.latLng) }
            .distinctUntilChanged()
            .flatMapLatest { latLng -> weatherLoadingUpdates(latLng, coroutineScope, intent) },
        intents.filterIsInstance<WeatherIntent.RetryLoadWeather>()
            .map { requireNotNull(currentState().event.venues?.firstOrNull()?.latLng) }
            .flatMapLatest { latLng -> weatherLoadingUpdates(latLng, coroutineScope, intent) }
    )

    private suspend fun weatherLoadingUpdates(
        latLng: LatLng,
        coroutineScope: CoroutineScope,
        intent: suspend (WeatherIntent) -> Unit
    ): Flow<WeatherStateUpdate> = flow<WeatherStateUpdate> {
        emit(WeatherStateUpdate.Weather.Loading)
        val resource = withContext(ioDispatcher) {
            getForecast(lat = latLng.latitude, lon = latLng.longitude)
        }
        emit(
            WeatherStateUpdate.Weather.Loaded(
                resource,
                retry = { coroutineScope.launch { intent(WeatherIntent.RetryLoadWeather) } }
            )
        )
    }
}
