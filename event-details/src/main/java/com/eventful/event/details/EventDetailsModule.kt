package com.eventful.event.details

import androidx.lifecycle.ViewModel
import com.eventful.core.android.base.savedStateViewModelFrom
import com.eventful.core.android.di.scope.FragmentScoped
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.InjectingSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.ViewModelKey
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@AssistedModule
@Module(includes = [AssistedInject_EventDetailsModule::class])
abstract class EventDetailsModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [EventDetailsViewModelModule::class])
    abstract fun eventDetailsFragment(): EventDetailsFragment

    @Binds
    @IntoMap
    @ViewModelKey(EventDetailsViewModel::class)
    abstract fun eventDetailsViewModelFactory(
        factory: EventDetailsViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    @Module
    object EventDetailsViewModelModule {
        @Provides
        fun eventDetailsViewModel(
            eventDetailsFragment: EventDetailsFragment,
            factory: InjectingSavedStateViewModelFactory
        ): EventDetailsViewModel = eventDetailsFragment.savedStateViewModelFrom(factory)
    }
}
