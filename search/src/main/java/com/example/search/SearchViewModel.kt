package com.example.search

import androidx.lifecycle.SavedStateHandle
import com.example.core.usecase.*
import com.example.coreandroid.base.FlowViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.util.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class SearchViewModel @AssistedInject constructor(
    processor: SearchFlowProcessor,
    @Assisted savedStateHandle: SavedStateHandle
) : FlowViewModel<SearchIntent, SearchStateUpdate, SearchState, SearchSignal>(
    initialState = savedStateHandle["initialState"] ?: SearchState(),
    processor = processor,
    savedStateHandle = savedStateHandle
) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<SearchViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): SearchViewModel
    }
}
