package com.example.weather

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.weather.Forecast
import com.example.core.repo.IWeatherRepository
import com.example.core.usecase.GetForecast
import com.example.core.util.flatMapFirst
import com.example.coreandroid.arch.BaseViewModel
import com.example.coreandroid.util.Loading
import com.google.android.gms.maps.model.LatLng
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed class WeatherIntent
class LoadWeather(val latLng: LatLng) : WeatherIntent()

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherVM(
    private val getForecast: GetForecast,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: WeatherState = WeatherState.INITIAL
) : BaseViewModel<WeatherIntent, WeatherState, Unit>(initialState) {
    init {
        intentsChannel.asFlow()
            .filterIsInstance<LoadWeather>()
            .flatMapFirst {
                flow {
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

class WeatherViewModel(
    private val repo: IWeatherRepository,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<WeatherState>(WeatherState.INITIAL) {

    fun loadWeather(latLng: LatLng) = withState { state ->
        if (state.forecast.status is Loading) return@withState

        viewModelScope.launch {
            setState { copy(forecast = forecast.copyWithLoadingInProgress) }
            when (val result = withContext(ioDispatcher) {
                repo.getForecast(lat = latLng.latitude, lon = latLng.longitude)
            }) {
                is Resource.Success -> setState {
                    copy(forecast = forecast.copyWithNewValue(result.data))
                }

                is Resource.Error<Forecast, *> -> setState {
                    copy(forecast = forecast.copyWithError(result.error))
                }
            }
        }
    }
}
