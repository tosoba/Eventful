package com.example.event

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.coreandroid.base.savedStateViewModelFrom
import com.example.coreandroid.di.fragment.FragmentKey
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@AssistedModule
@Module(includes = [AssistedInject_EventModule::class])
abstract class EventModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [EventViewModelModule::class])
    abstract fun eventFragment(): EventFragment


    @Binds
    @IntoMap
    @ViewModelKey(EventViewModel::class)
    abstract fun eventViewModelFactory(
        factory: EventViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    @Module
    object EventViewModelModule {
        @Provides
        fun eventViewModel(
            eventFragment: EventFragment,
            factory: InjectingSavedStateViewModelFactory
        ): EventViewModel = eventFragment.savedStateViewModelFrom(factory)
    }
}
