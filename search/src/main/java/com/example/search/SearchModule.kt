package com.example.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.*
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.core.provider.ConnectedStateProvider
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
abstract class SearchModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [ModuleProvides::class])
    abstract fun searchFragment(): SearchFragment

    @Module
    class ModuleProvides {

        @Provides
        @IntoMap
        @ViewModelKey(SearchViewModel::class)
        fun searchViewModelBase(
            searchEvents: SearchEvents,
            getPagedEventsFlow: GetPagedEventsFlow,
            saveEvents: SaveEvents,
            getSearchSuggestions: GetSearchSuggestions,
            saveSearchSuggestion: SaveSearchSuggestion,
            connectedStateProvider: ConnectedStateProvider,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = SearchViewModel(
            searchEvents,
            getPagedEventsFlow,
            saveEvents,
            getSearchSuggestions,
            saveSearchSuggestion,
            connectedStateProvider,
            ioDispatcher
        )

        @Provides
        fun searchViewModel(
            factory: ViewModelProvider.Factory,
            target: SearchFragment
        ): SearchViewModel = ViewModelProvider(target, factory).get(SearchViewModel::class.java)
    }
}