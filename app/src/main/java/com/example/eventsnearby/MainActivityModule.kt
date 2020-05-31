package com.example.eventsnearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetConnection
import com.example.core.usecase.GetLocation
import com.example.core.usecase.GetLocationAvailability
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.ActivityScoped
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.event.EventModule
import com.example.favourites.FavouritesModule
import com.example.nearby.NearbyModule
import com.example.search.SearchModule
import com.example.weather.WeatherModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module(includes = [MainActivityModule.Providers::class])
abstract class MainActivityModule {

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SubProviders::class,
            NearbyModule::class,
            SearchModule::class,
            FavouritesModule::class,
            EventModule::class,
            WeatherModule::class,
            MainFragmentModule::class
        ]
    )
    abstract fun mainActivity(): MainActivity

    @Binds
    abstract fun connectivityStateProvider(mainViewModel: MainViewModel): ConnectedStateProvider

    @Binds
    abstract fun locationStateProvider(mainViewModel: MainViewModel): LocationStateProvider

    @Module
    class Providers {

        @Provides
        @IntoMap
        @ViewModelKey(MainViewModel::class)
        fun mainViewModel(
            getLocation: GetLocation,
            getLocationAvailability: GetLocationAvailability,
            getConnection: GetConnection
        ): ViewModel = MainViewModel(getLocation, getLocationAvailability, getConnection)
    }

    @Module
    class SubProviders {

        @Provides
        fun mainViewModel(
            factory: ViewModelProvider.Factory, target: MainActivity
        ): MainViewModel = ViewModelProvider(target, factory).get(MainViewModel::class.java)
    }
}