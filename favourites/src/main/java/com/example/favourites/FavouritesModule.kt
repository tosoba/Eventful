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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
abstract class FavouritesModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [ModuleProvides::class])
    abstract fun favouritesFragment(): FavouritesFragment

    @Module
    class ModuleProvides {

        @Provides
        @IntoMap
        @ViewModelKey(FavouritesVM::class)
        fun favouritesViewModelBase(
            getSavedEvents: GetSavedEvents,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = FavouritesVM(getSavedEvents, ioDispatcher)

        @Provides
        fun favouritesViewModel(
            factory: ViewModelProvider.Factory, target: FavouritesFragment
        ): FavouritesVM = ViewModelProvider(target, factory)
            .get(FavouritesVM::class.java)
    }
}