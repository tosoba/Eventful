package com.example.weather

import com.example.core.usecase.GetForecast
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.FlowProcessor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
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
        states: StateFlow<WeatherState>,
        intent: suspend (WeatherIntent) -> Unit,
        signal: suspend (Unit) -> Unit
    ): Flow<WeatherStateUpdate> = intents.filterIsInstance<WeatherIntent.LoadWeather>()
        .flatMapFirst {
            flow<WeatherStateUpdate> {
                emit(WeatherStateUpdate.Weather.Loading)
                val resource = withContext(ioDispatcher) {
                    getForecast(
                        lat = it.latLng.latitude,
                        lon = it.latLng.longitude
                    )
                }
                emit(WeatherStateUpdate.Weather.Loaded(resource))
            }
        }
}
