package com.example.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.DeleteEvents
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
        @ViewModelKey(FavouritesViewModel::class)
        fun favouritesViewModelBase(
            getSavedEvents: GetSavedEvents,
            deleteEvents: DeleteEvents,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = FavouritesViewModel(getSavedEvents, deleteEvents, ioDispatcher)

        @Provides
        fun favouritesViewModel(
            factory: ViewModelProvider.Factory, target: FavouritesFragment
        ): FavouritesViewModel = ViewModelProvider(target, factory)
            .get(FavouritesViewModel::class.java)
    }
}