package com.example.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.core.IEventsRepository
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


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
        fun nearbyActionsProvider(
            repository: IEventsRepository
        ): NearbyActionsProvider = NearbyActionsProvider(repository)

        @Provides
        @IntoMap
        @ViewModelKey(NearbyViewModel::class)
        fun nearbyViewModel(
            nearbyActionsProvider: NearbyActionsProvider
        ): ViewModel = NearbyViewModel(nearbyActionsProvider)
    }

    @Module
    class SubProviders {

        @Provides
        fun nearbyViewModel(
            factory: ViewModelProvider.Factory,
            target: NearbyFragment
        ): NearbyViewModel = ViewModelProviders.of(target, factory).get(NearbyViewModel::class.java)
    }

}