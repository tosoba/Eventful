package com.example.weather

import androidx.lifecycle.SavedStateHandle
import com.example.core.model.Resource
import com.example.core.model.weather.Forecast
import com.example.core.usecase.GetForecast
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.util.StateUpdate
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherViewModel @AssistedInject constructor(
    private val getForecast: GetForecast,
    private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<WeatherIntent, WeatherState, Unit>(
    savedStateHandle["initialState"] ?: WeatherState()
) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<WeatherViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): WeatherViewModel
    }

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
            .applyToState(initialState = savedStateHandle["initialState"] ?: WeatherState())
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
