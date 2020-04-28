package com.example.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveEvents
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.provider.ConnectivityStateProvider
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
            saveEvents: SaveEvents,
            getSeachSuggestions: GetSeachSuggestions,
            saveSuggestion: SaveSuggestion,
            connectivityStateProvider: ConnectivityStateProvider,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = SearchViewModel(
            searchEvents,
            saveEvents,
            getSeachSuggestions,
            saveSuggestion,
            connectivityStateProvider,
            ioDispatcher
        )

        @Provides
        fun searchViewModel(
            factory: ViewModelProvider.Factory,
            target: SearchFragment
        ): SearchViewModel = ViewModelProvider(target, factory).get(SearchViewModel::class.java)
    }
}