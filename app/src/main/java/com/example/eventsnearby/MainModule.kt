package com.example.eventsnearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.di.ViewModelKey
import com.example.nearby.NearbyModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(
    includes = [
        MainModule.ProvideViewModel::class
    ]
)
abstract class MainModule {

    @ContributesAndroidInjector(
        modules = [
            InjectViewModel::class,
            NearbyModule::class
        ]
    )
    abstract fun bind(): MainActivity

    @Binds
    abstract fun connectivityStateProvider(mainViewModel: MainViewModel): ConnectivityStateProvider

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(MainViewModel::class)
        fun mainViewModel(): ViewModel = MainViewModel()
    }

    @Module
    class InjectViewModel {

        @Provides
        fun mainViewModel(
            factory: ViewModelProvider.Factory,
            target: MainActivity
        ): MainViewModel = ViewModelProviders.of(target, factory).get(MainViewModel::class.java)
    }
}