package com.example.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetForecast
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module(includes = [WeatherModule.Providers::class])
abstract class WeatherModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [SubProviders::class])
    abstract fun weatherFragment(): WeatherFragment

    @Module
    class Providers {

        @Provides
        @IntoMap
        @ViewModelKey(WeatherViewModel::class)
        fun weatherViewModel(
            getForecast: GetForecast,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = WeatherViewModel(getForecast, ioDispatcher)
    }

    @Module
    class SubProviders {

        @Provides
        fun weatherViewModel(
            factory: ViewModelProvider.Factory,
            target: WeatherFragment
        ): WeatherViewModel = ViewModelProvider(target, factory).get(WeatherViewModel::class.java)
    }
}