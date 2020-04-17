package com.example.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.coreandroid.base.ConnectivityStateProvider
import com.example.coreandroid.base.LocationStateProvider
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
@Module
abstract class NearbyModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [ModuleProvides::class])
    abstract fun nearbyFragment(): NearbyFragment

    @Module
    class ModuleProvides {

        @Provides
        @IntoMap
        @ViewModelKey(NearbyViewModel::class)
        fun nearbyViewModelBase(
            getNearbyEvents: GetNearbyEvents,
            saveEvents: SaveEvents,
            connectivityStateProvider: ConnectivityStateProvider,
            locationStateProvider: LocationStateProvider,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = NearbyViewModel(
            getNearbyEvents,
            saveEvents,
            connectivityStateProvider,
            locationStateProvider,
            ioDispatcher
        )

        @Provides
        fun nearbyViewModel(
            factory: ViewModelProvider.Factory,
            target: NearbyFragment
        ): NearbyViewModel = ViewModelProvider(target, factory).get(NearbyViewModel::class.java)
    }
}