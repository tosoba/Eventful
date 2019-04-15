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
        NearbyModule.ProvideViewModel::class
    ]
)
abstract class NearbyModule {

    @FragmentScoped
    @ContributesAndroidInjector(
        modules = [
            InjectViewModel::class
        ]
    )
    abstract fun bind(): NearbyFragment

    @Module
    class ProvideViewModel {

        @Provides
        fun nearbyActionsProvider(
            repository: IEventsRepository
        ): NearbyActionsProvider = NearbyActionsProvider(repository)

        @Provides
        @IntoMap
        @ViewModelKey(NearbyViewModel::class)
        fun provideNearbyViewModel(
            nearbyActionsProvider: NearbyActionsProvider
        ): ViewModel = NearbyViewModel(nearbyActionsProvider)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideNearbyViewModel(
            factory: ViewModelProvider.Factory,
            target: NearbyFragment
        ): NearbyViewModel = ViewModelProviders.of(target, factory).get(NearbyViewModel::class.java)
    }

}