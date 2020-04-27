package com.example.coreandroid.util

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SelectableEventsState<S : SelectableEventsState<S>> {
    val events: HoldsData<List<Selectable<Event>>>
    fun copyWithTransformedEvents(transform: (Selectable<Event>) -> Selectable<Event>): S
}

interface ClearEventSelectionIntent

interface EventSelectionToggledIntent {
    val event: Event
}

fun <I : ClearEventSelectionIntent, S : SelectableEventsState<S>> Flow<I>.processClearSelectionIntents(
    currentState: () -> S
): Flow<S> = map { currentState().copyWithTransformedEvents { it.copy(selected = false) } }

fun <I : EventSelectionToggledIntent, S : SelectableEventsState<S>> Flow<I>.processEventLongClickedIntents(
    currentState: () -> S
): Flow<S> = map { intent ->
    currentState().copyWithTransformedEvents {
        if (it.item.id == intent.event.id) Selectable(intent.event, !it.selected) else it
    }
}
