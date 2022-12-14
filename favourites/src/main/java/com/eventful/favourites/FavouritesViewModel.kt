package com.eventful.favourites

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowViewModel
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesViewModel
@AssistedInject
constructor(processor: FavouritesFlowProcessor, @Assisted savedStateHandle: SavedStateHandle) :
    FlowViewModel<FavouritesIntent, FavouritesStateUpdate, FavouritesState, FavouritesSignal>(
        initialState = savedStateHandle["initialState"] ?: FavouritesState(),
        processor = processor,
        savedStateHandle = savedStateHandle) {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<FavouritesViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): FavouritesViewModel
    }
}
