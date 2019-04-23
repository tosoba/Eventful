package com.example.eventsnearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.ActivityScoped
import com.example.nearby.NearbyModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(
    includes = [
        MainActivityModule.Providers::class
    ]
)
abstract class MainActivityModule {

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SubProviders::class,
            NearbyModule::class,
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
        fun mainActionsProvider(): MainActionsProvider = MainActionsProvider()

        @Provides
        @IntoMap
        @ViewModelKey(MainViewModel::class)
        fun mainViewModel(
            mainActionsProvider: MainActionsProvider
        ): ViewModel = MainViewModel(mainActionsProvider)
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