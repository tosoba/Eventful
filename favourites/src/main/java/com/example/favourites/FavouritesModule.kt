package com.example.favourites

import androidx.lifecycle.ViewModel
import com.example.core.usecase.event.DeleteEvents
import com.example.core.usecase.event.GetSavedEventsFlow
import com.example.coreandroid.base.savedStateViewModelFrom
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.ViewModelKey
import com.squareup.inject.assisted.dagger2.AssistedModule
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
@AssistedModule
@Module(includes = [AssistedInject_FavouritesModule::class])
abstract class FavouritesModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [FavouritesViewModelModule::class])
    abstract fun favouritesFragment(): FavouritesFragment

    @Binds
    @IntoMap
    @ViewModelKey(FavouritesViewModel::class)
    abstract fun favouritesViewModelFactory(
        factory: FavouritesViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    companion object {
        @Provides
        fun favouritesFlowProcessor(
            getSavedEventsFlow: GetSavedEventsFlow,
            deleteEvents: DeleteEvents,
            ioDispatcher: CoroutineDispatcher
        ): FavouritesFlowProcessor = FavouritesFlowProcessor(
            getSavedEventsFlow,
            deleteEvents,
            ioDispatcher
        )
    }

    @Module
    object FavouritesViewModelModule {
        @Provides
        fun favouritesViewModel(
            favouritesFragment: FavouritesFragment,
            factory: InjectingSavedStateViewModelFactory
        ): FavouritesViewModel = favouritesFragment.savedStateViewModelFrom(factory)
    }
}
