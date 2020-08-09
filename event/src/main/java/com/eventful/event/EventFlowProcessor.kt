package com.eventful.event

import com.eventful.core.android.base.FlowProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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
            .map { (event) -> EventStateUpdate.AddEvent(event) }
    )
}