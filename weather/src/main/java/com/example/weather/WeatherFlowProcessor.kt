package com.example.weather

import com.example.core.usecase.GetForecast
import com.example.coreandroid.base.FlowProcessor
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherFlowProcessor @Inject constructor(
    private val getForecast: GetForecast,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<WeatherIntent, WeatherStateUpdate, WeatherState, Unit> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<WeatherIntent>,
        currentState: () -> WeatherState,
        states: Flow<WeatherState>,
        intent: suspend (WeatherIntent) -> Unit,
        signal: suspend (Unit) -> Unit
    ): Flow<WeatherStateUpdate> = merge(
        states.map { it.latLng }
            .filterNotNull()
            .take(1)
            .flatMapLatest { weatherLoadingUpdates(it, coroutineScope, intent) },
        intents.filterIsInstance<WeatherIntent.RetryLoadWeather>()
            .map { currentState().latLng }
            .filterNotNull()
            .flatMapLatest { weatherLoadingUpdates(it, coroutineScope, intent) }
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
