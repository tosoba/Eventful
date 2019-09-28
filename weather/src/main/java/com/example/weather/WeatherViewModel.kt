package com.example.weather

import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.core.model.weather.Forecast
import com.example.core.repo.IWeatherRepository
import com.example.coreandroid.util.Loading
import com.google.android.gms.maps.model.LatLng
import com.haroldadmin.vector.VectorViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherViewModel(
    private val repo: IWeatherRepository,
    private val ioDispatcher: CoroutineDispatcher
) : VectorViewModel<WeatherState>(WeatherState.INITIAL) {

    fun loadWeather(latLng: LatLng) = viewModelScope.launch {
        withState { state ->
            if (state.forecast.status is Loading) return@withState

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
