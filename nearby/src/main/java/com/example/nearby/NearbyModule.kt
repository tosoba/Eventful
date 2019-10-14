package com.example.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.SaveEvents
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher


@Module(
    includes = [
        NearbyModule.Providers::class
    ]
)
abstract class NearbyModule {

    @FragmentScoped
    @ContributesAndroidInjector(
        modules = [
            SubProviders::class
        ]
    )
    abstract fun nearbyFragment(): NearbyFragment

    @Module
    class Providers {

        @Provides
        @IntoMap
        @ViewModelKey(NearbyViewModel::class)
        fun nearbyViewModel(
            getNearbyEvents: GetNearbyEvents,
            saveEvents: SaveEvents,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = NearbyViewModel(getNearbyEvents, saveEvents, ioDispatcher)
    }

    @Module
    class SubProviders {

        @Provides
        fun nearbyViewModel(
            factory: ViewModelProvider.Factory,
            target: NearbyFragment
        ): NearbyViewModel = ViewModelProvider(target, factory).get(NearbyViewModel::class.java)
    }
}