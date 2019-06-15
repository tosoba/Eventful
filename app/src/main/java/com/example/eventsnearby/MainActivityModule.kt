package com.example.eventsnearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.ActivityScoped
import com.example.nearby.NearbyModule
import com.example.weather.WeatherModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import io.nlopez.smartlocation.SmartLocation

@Module(
    includes = [MainActivityModule.Providers::class]
)
abstract class MainActivityModule {

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SubProviders::class,
            NearbyModule::class,
            WeatherModule::class,
            MainFragmentModule::class
        ]
    )
    abstract fun mainActivity(): MainActivity

    @Binds
    abstract fun connectivityStateProvider(mainViewModel: MainViewModel): ConnectivityStateProvider

    @Binds
    abstract fun locationStateProvider(mainViewModel: MainViewModel): LocationStateProvider

    @Module
    class Providers {

        @Provides
        @IntoMap
        @ViewModelKey(MainViewModel::class)
        fun mainViewModel(
            smartLocation: SmartLocation
        ): ViewModel = MainViewModel(smartLocation)
    }

    @Module
    class SubProviders {

        @Provides
        fun mainViewModel(
            factory: ViewModelProvider.Factory,
            target: MainActivity
        ): MainViewModel = ViewModelProviders.of(target, factory).get(MainViewModel::class.java)
    }
}