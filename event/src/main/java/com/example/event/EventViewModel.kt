package com.example.event

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSaved
import com.example.core.usecase.SaveEvent
import com.example.core.util.flatMapFirst
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.util.Data
import com.example.coreandroid.util.Initial
import com.example.coreandroid.util.LoadedSuccessfully
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class EventViewModel(
    initialState: EventState,
    private val isEventSaved: IsEventSaved,
    private val saveEvent: SaveEvent,
    private val deleteEvent: DeleteEvent
) : BaseViewModel<EventIntent, EventState, EventSignal>(initialState) {

    init {
        merge(
            intentsWithLatestStates.flatMapConcat { (intent, state) ->
                when (intent) {
                    is ToggleFavourite -> flowOf(intent).flatMapFirst {
                        viewModelScope.launch {
                            if (state.isFavourite.data) deleteEvent(state.event)
                            else saveEvent(state.event)
                        }
                        flowOf(state.copy(isFavourite = state.isFavourite.copyWithLoadingStatus))
                    }
                }
            },
            statesChannel.asFlow()
                .map { it.event.id }
                .distinctUntilChanged()
                .flatMapLatest { isEventSaved(it) }
                .map {
                    state.run {
                        if (isFavourite.status !is Initial)
                            liveSignals.value = EventSignal.FavouriteStateToggled(it)
                        copy(isFavourite = Data(it, LoadedSuccessfully))
                    }
                }
        ).onEach(statesChannel::send).launchIn(viewModelScope)
    }
}
