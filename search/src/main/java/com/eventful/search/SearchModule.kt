package com.eventful.search

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
@Module(includes = [AssistedInject_SearchModule::class])
abstract class SearchModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [SearchViewModelModule::class])
    abstract fun searchFragment(): SearchFragment

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun searchViewModelFactory(
        factory: SearchViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    @Module
    object SearchViewModelModule {
        @Provides
        fun searchViewModel(
            searchFragment: SearchFragment,
            factory: InjectingSavedStateViewModelFactory
        ): SearchViewModel = searchFragment.savedStateViewModelFrom(factory)
    }
}
