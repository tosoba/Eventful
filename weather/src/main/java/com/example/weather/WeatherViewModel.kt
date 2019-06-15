package com.example.weather

import com.example.core.Failure
import com.example.core.IWeatherRepository
import com.example.core.Success
import com.example.coreandroid.arch.state.ViewStateStore
import com.example.coreandroid.base.CoroutineViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherViewModel(
    private val repo: IWeatherRepository,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineViewModel<WeatherState>(ViewStateStore(WeatherState.INITIAL)) {

    fun loadWeather(latlng: LatLng) {
        launch {
            viewStateStore.dispatchStateTransition { copy(forecastState = ForecastState.Loading) }
            when (val result = withContext(ioDispatcher) {
                repo.getForecast(lat = latlng.latitude, lon = latlng.longitude)
            }) {
                is Success -> viewStateStore.dispatchStateTransition {
                    copy(forecastState = ForecastState.Found(result.data))
                }

                is Failure -> viewStateStore.dispatchStateTransition {
                    copy(forecastState = ForecastState.Error(result.error))
                }
            }
        }
    }
}