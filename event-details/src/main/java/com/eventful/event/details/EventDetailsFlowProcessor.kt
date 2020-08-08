package com.eventful.event.details

import com.eventful.core.android.base.FlowProcessor
import com.eventful.core.android.provider.CurrentEventProvider
import com.eventful.core.usecase.event.DeleteEvent
import com.eventful.core.usecase.event.IsEventSavedFlow
import com.eventful.core.usecase.event.SaveEvent
import com.eventful.core.util.Initial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventDetailsFlowProcessor @Inject constructor(
    private val isEventSavedFlow: IsEventSavedFlow,
    private val saveEvent: SaveEvent,
    private val deleteEvent: DeleteEvent,
    private val currentEventProvider: CurrentEventProvider
) : FlowProcessor<EventDetailsIntent, EventDetailsStateUpdate, EventDetailsState, EventDetailsSignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<EventDetailsIntent>,
        currentState: () -> EventDetailsState,
        states: Flow<EventDetailsState>,
        intent: suspend (EventDetailsIntent) -> Unit,
        signal: suspend (EventDetailsSignal) -> Unit
    ): Flow<EventDetailsStateUpdate> = merge(
        intents.filterIsInstance<EventDetailsIntent.ToggleFavourite>()
            .filter { currentState().isFavourite.data != null }
            .onEach {
                coroutineScope.launch {
                    currentState().run {
                        if (isFavourite.data!!) deleteEvent(event)
                        else saveEvent(event)
                    }
                }
            }
            .map { EventDetailsStateUpdate.FavouriteStatus.Loading },
        states.map { it.event.id }
            .distinctUntilChanged()
            .flatMapLatest { isEventSavedFlow(it) }
            .map {
                if (currentState().isFavourite.status !is Initial)
                    signal(EventDetailsSignal.FavouriteStateToggled(it))
                EventDetailsStateUpdate.FavouriteStatus.Loaded(favourite = it)
            }
    )
}
