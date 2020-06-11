package com.example.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSavedFlow
import com.example.core.usecase.SaveEvent
import com.example.core.util.Initial
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class EventViewModel @AssistedInject constructor(
    private val isEventSavedFlow: IsEventSavedFlow,
    private val saveEvent: SaveEvent,
    private val deleteEvent: DeleteEvent,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<EventIntent, EventStateUpdate, EventState, EventSignal>(savedStateHandle["initialState"]!!) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<EventViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): EventViewModel
    }

    init {
        start()
    }

    override val updates: Flow<EventStateUpdate>
        get() = merge(
            intents.filterIsInstance<ToggleFavourite>()
                .onEach {
                    viewModelScope.launch {
                        state.run {
                            if (isFavourite.data) deleteEvent(event)
                            else saveEvent(event)
                        }
                    }
                }
                .map { EventStateUpdate.FavouriteStatus.Loading },
            states.map { it.event.id }
                .distinctUntilChanged()
                .flatMapLatest { isEventSavedFlow(it) }
                .map {
                    if (state.isFavourite.status !is Initial)
                        signal(EventSignal.FavouriteStateToggled(it))
                    EventStateUpdate.FavouriteStatus.Loaded(favourite = it)
                }
        )
}
