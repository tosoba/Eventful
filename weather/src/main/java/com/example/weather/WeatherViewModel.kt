package com.example.weather

import androidx.lifecycle.SavedStateHandle
import com.example.coreandroid.base.FlowViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherViewModel @AssistedInject constructor(
    processor: WeatherFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : FlowViewModel<WeatherIntent, WeatherStateUpdate, WeatherState, Unit>(
    initialState = WeatherState(latLng = savedStateHandle[WeatherFragment.LAT_LNG_ARG_KEY]),
    processor = processor,
    savedStateHandle = savedStateHandle
) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<WeatherViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): WeatherViewModel
    }
}
