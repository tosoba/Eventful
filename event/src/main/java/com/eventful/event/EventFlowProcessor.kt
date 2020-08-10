package com.eventful.event

import com.eventful.core.android.base.FlowProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class EventFlowProcessor @Inject constructor() :
    FlowProcessor<EventIntent, EventStateUpdate, EventState, Unit> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<EventIntent>,
        currentState: () -> EventState,
        states: Flow<EventState>,
        intent: suspend (EventIntent) -> Unit,
        signal: suspend (Unit) -> Unit
    ): Flow<EventStateUpdate> = merge(
        intents.filterIsInstance<EventIntent.BackPressed>()
            .map { EventStateUpdate.DropLastEvent },
        intents.filterIsInstance<EventIntent.NewEvent>()
            .filterNot { (event) -> currentState().events.lastOrNull() == event }
            .map { (event) -> EventStateUpdate.AddEvent(event) }
    )
}