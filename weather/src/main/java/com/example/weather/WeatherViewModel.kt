package com.example.weather

import androidx.lifecycle.SavedStateHandle
import com.example.core.usecase.GetForecast
import com.example.core.util.ext.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherViewModel @AssistedInject constructor(
    private val getForecast: GetForecast,
    private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<WeatherIntent, WeatherStateUpdate, WeatherState, Unit>(
    savedStateHandle["initialState"] ?: WeatherState()
) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<WeatherViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): WeatherViewModel
    }

    init {
        start()
    }

    override val updates: Flow<WeatherStateUpdate>
        get() = intents.filterIsInstance<LoadWeather>()
            .flatMapFirst { intent ->
                flow<WeatherStateUpdate> {
                    emit(WeatherStateUpdate.Weather.Loading)
                    val resource = withContext(ioDispatcher) {
                        getForecast(
                            lat = intent.latLng.latitude,
                            lon = intent.latLng.longitude
                        )
                    }
                    emit(WeatherStateUpdate.Weather.Loaded(resource))
                }
            }
}
