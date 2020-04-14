package com.example.eventsnearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetConnection
import com.example.core.usecase.GetLocation
import com.example.core.usecase.GetLocationAvailability
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.ActivityScoped
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

@Module(
    includes = [MainActivityModule.Providers::class]
)
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
    abstract fun connectivityStateProvider(mainViewModel: MainVM): ConnectivityStateProvider

    @Binds
    abstract fun locationStateProvider(mainViewModel: MainVM): LocationStateProvider

    @Module
    class Providers {

        @Provides
        @IntoMap
        @ViewModelKey(MainVM::class)
        fun mainViewModel(
            getLocation: GetLocation,
            getLocationAvailability: GetLocationAvailability,
            getConnection: GetConnection
        ): ViewModel = MainVM(getLocation, getLocationAvailability, getConnection)
    }

    @Module
    class SubProviders {

        @Provides
        fun mainViewModel(
            factory: ViewModelProvider.Factory, target: MainActivity
        ): MainVM = ViewModelProvider(target, factory).get(MainVM::class.java)
    }
}