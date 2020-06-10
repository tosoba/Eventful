package com.example.weather

import androidx.lifecycle.ViewModel
import com.example.core.usecase.GetForecast
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.di.viewmodel.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
abstract class WeatherModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun weatherFragment(): WeatherFragment

    companion object {
        @Provides
        @IntoMap
        @ViewModelKey(WeatherViewModel::class)
        fun weatherViewModel(
            getForecast: GetForecast,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = WeatherViewModel(getForecast, ioDispatcher)
    }
}