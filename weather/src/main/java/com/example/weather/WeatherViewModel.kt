package com.example.weather

import com.example.core.Resource
import com.example.core.model.weather.Forecast
import com.example.core.usecase.GetForecast
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.util.StateUpdate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherViewModel(
    private val getForecast: GetForecast,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: WeatherState = WeatherState()
) : BaseViewModel<WeatherIntent, WeatherState, Unit>(initialState) {
    init {
        intents.filterIsInstance<LoadWeather>()
            .flatMapFirst { intent ->
                flow<Update> {
                    emit(Update.Weather.Loading)
                    val resource = withContext(ioDispatcher) {
                        getForecast(
                            lat = intent.latLng.latitude,
                            lon = intent.latLng.longitude
                        )
                    }
                    emit(Update.Weather.Loaded(resource))
                }
            }
            .applyToState(initialState = initialState)
    }

    private sealed class Update : StateUpdate<WeatherState> {
        sealed class Weather : Update() {
            object Loading : Weather() {
                override fun invoke(state: WeatherState): WeatherState = state.copy(
                    forecast = state.forecast.copyWithLoadingStatus
                )
            }

            class Loaded(private val resource: Resource<Forecast>) : Weather() {
                override fun invoke(state: WeatherState): WeatherState = when (resource) {
                    is Resource.Success -> state.copy(
                        forecast = state.forecast.copyWithNewValue(resource.data)
                    )
                    is Resource.Error<Forecast> -> state.copy(
                        forecast = state.forecast.copyWithFailureStatus(resource.error)
                    )
                }
            }
        }
    }
}
