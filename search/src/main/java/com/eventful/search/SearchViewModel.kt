package com.eventful.search

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class SearchViewModel
@AssistedInject
constructor(processor: SearchFlowProcessor, @Assisted savedStateHandle: SavedStateHandle) :
    FlowViewModel<SearchIntent, SearchStateUpdate, SearchState, SearchSignal>(
        initialState = SearchState(savedStateHandle),
        processor = processor,
        savedStateHandle = savedStateHandle) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<SearchViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): SearchViewModel
    }
}
