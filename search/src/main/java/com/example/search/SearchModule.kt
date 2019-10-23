package com.example.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher

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
            getSeachSuggestions: GetSeachSuggestions,
            saveSuggestion: SaveSuggestion,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = SearchViewModel(
            searchEvents, getSeachSuggestions, saveSuggestion, ioDispatcher
        )

        @Provides
        fun searchViewModel(
            factory: ViewModelProvider.Factory,
            target: SearchFragment
        ): SearchViewModel = ViewModelProvider(target, factory).get(SearchViewModel::class.java)
    }
}