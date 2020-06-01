package com.example.event

import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSaved
import com.example.core.usecase.SaveEvent
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.util.Data
import com.example.coreandroid.util.Initial
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.StateUpdate
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
            intents.filterIsInstance<ToggleFavourite>()
                .onEach {
                    viewModelScope.launch {
                        state.run {
                            if (isFavourite.data) deleteEvent(event)
                            else saveEvent(event)
                        }
                    }
                }
                .map { Update.FavouriteStatus.Loading },
            states.map { it.event.id }
                .distinctUntilChanged()
                .flatMapLatest { isEventSaved(it) }
                .map {
                    if (state.isFavourite.status !is Initial)
                        signal(EventSignal.FavouriteStateToggled(it))
                    Update.FavouriteStatus.Loaded(favourite = it)
                }
        ).applyToState(initialState = initialState)
    }

    private sealed class Update : StateUpdate<EventState> {
        sealed class FavouriteStatus : Update() {
            object Loading : FavouriteStatus() {
                override fun invoke(state: EventState): EventState = state.copy(
                    isFavourite = state.isFavourite.copyWithLoadingStatus
                )
            }

            class Loaded(private val favourite: Boolean) : FavouriteStatus() {
                override fun invoke(state: EventState): EventState = state.copy(
                    isFavourite = Data(favourite, LoadedSuccessfully)
                )
            }
        }
    }
}
