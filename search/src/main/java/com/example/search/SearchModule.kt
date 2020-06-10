package com.example.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.provider.ConnectedStateProvider
import com.example.core.usecase.*
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
abstract class SearchModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun searchFragment(): SearchFragment

    companion object {
        @Provides
        @IntoMap
        @ViewModelKey(SearchViewModel::class)
        fun searchViewModel(
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
    }
}