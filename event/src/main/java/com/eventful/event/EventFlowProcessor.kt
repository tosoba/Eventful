package com.eventful.event

import com.eventful.core.usecase.event.DeleteEvent
import com.eventful.core.usecase.event.IsEventSavedFlow
import com.eventful.core.usecase.event.SaveEvent
import com.eventful.core.util.Initial
import com.eventful.core.android.base.FlowProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventFlowProcessor @Inject constructor(
    private val isEventSavedFlow: IsEventSavedFlow,
    private val saveEvent: SaveEvent,
    private val deleteEvent: DeleteEvent
) : FlowProcessor<EventIntent, EventStateUpdate, EventState, EventSignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<EventIntent>,
        currentState: () -> EventState,
        states: Flow<EventState>,
        intent: suspend (EventIntent) -> Unit,
        signal: suspend (EventSignal) -> Unit
    ): Flow<EventStateUpdate> = merge(
        intents.filterIsInstance<EventIntent.ToggleFavourite>()
            .filter { currentState().isFavourite.data != null }
            .onEach {
                coroutineScope.launch {
                    currentState().run {
                        if (isFavourite.data!!) deleteEvent(event)
                        else saveEvent(event)
                    }
                }
            }
            .map { EventStateUpdate.FavouriteStatus.Loading },
        states.map { it.event.id }
            .distinctUntilChanged()
            .flatMapLatest { isEventSavedFlow(it) }
            .map {
                if (currentState().isFavourite.status !is Initial)
                    signal(EventSignal.FavouriteStateToggled(it))
                EventStateUpdate.FavouriteStatus.Loaded(favourite = it)
            }
    )
}
