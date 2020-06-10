package com.example.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import dagger.Binds
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
    @ContributesAndroidInjector
    abstract fun favouritesFragment(): FavouritesFragment

    companion object {
        @Provides
        @IntoMap
        @ViewModelKey(FavouritesViewModel::class)
        fun favouritesViewModel(
            getSavedEventsFlow: GetSavedEventsFlow,
            deleteEvents: DeleteEvents,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = FavouritesViewModel(getSavedEventsFlow, deleteEvents, ioDispatcher)
    }
}