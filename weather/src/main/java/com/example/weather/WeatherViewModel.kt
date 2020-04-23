package com.example.weather

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.weather.Forecast
import com.example.core.usecase.GetForecast
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherViewModel(
    private val getForecast: GetForecast,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: WeatherState = WeatherState.INITIAL
) : BaseViewModel<WeatherIntent, WeatherState, Unit>(initialState) {
    init {
        intentsChannel.asFlow()
            .filterIsInstance<LoadWeather>()
            .flatMapFirst {
                flow<WeatherState> {
                    val state = statesChannel.value
                    emit(state.copy(forecast = state.forecast.copyWithLoadingInProgress))
                    when (val result = withContext(ioDispatcher) {
                        getForecast(lat = it.latLng.latitude, lon = it.latLng.longitude)
                    }) {
                        is Resource.Success -> state.copy(
                            forecast = state.forecast.copyWithNewValue(result.data)
                        )
                        is Resource.Error<Forecast, *> -> state.copy(
                            forecast = state.forecast.copyWithError(result.error)
                        )
                    }
                }
            }
            .onEach(statesChannel::send)
            .launchIn(viewModelScope)
    }
}
