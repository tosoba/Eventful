package com.example.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher

@Module(
    includes = [
        FavouritesModule.Providers::class
    ]
)
abstract class FavouritesModule {

    @FragmentScoped
    @ContributesAndroidInjector(
        modules = [
            SubProviders::class
        ]
    )
    abstract fun favouritesFragment(): FavouritesFragment

    @Module
    class Providers {

        @Provides
        @IntoMap
        @ViewModelKey(FavouritesViewModel::class)
        fun favouritesViewModel(
            getSavedEvents: GetSavedEvents,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = FavouritesViewModel(getSavedEvents, ioDispatcher)
    }

    @Module
    class SubProviders {

        @Provides
        fun favouritesViewModel(
            factory: ViewModelProvider.Factory, target: FavouritesFragment
        ): FavouritesViewModel = ViewModelProvider(target, factory)
            .get(FavouritesViewModel::class.java)
    }
}