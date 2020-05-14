package com.example.coreandroid.util

import com.example.core.usecase.SaveEvents
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SelectableEventsState<S : SelectableEventsState<S>> {
    val events: HoldsData<List<Selectable<Event>>>
    fun copyWithTransformedEvents(transform: (Selectable<Event>) -> Selectable<Event>): S
}

interface ClearEventSelectionIntent

interface EventSelectionToggledIntent {
    val event: Event
}

interface AddToFavouritesIntent

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

fun <I : ClearEventSelectionIntent, S : SelectableEventsState<S>> Flow<Pair<I, S>>.processClearSelectionIntents(): Flow<S> {
    return map { (_, state) -> state.copyWithTransformedEvents { it.copy(selected = false) } }
}

fun <I : EventSelectionToggledIntent, S : SelectableEventsState<S>> Flow<Pair<I, S>>.processEventLongClickedIntents(): Flow<S> {
    return map { (intent, state) ->
        state.copyWithTransformedEvents {
            if (it.item.id == intent.event.id) Selectable(intent.event, !it.selected) else it
        }
    }
}

fun <I : AddToFavouritesIntent, S : SelectableEventsState<S>> Flow<Pair<I, S>>.processAddToFavouritesIntents(
    saveEvents: SaveEvents,
    ioDispatcher: CoroutineDispatcher,
    sideEffect: (() -> Unit)? = null
): Flow<S> = map { (_, currentState) ->
    withContext(ioDispatcher) {
        saveEvents(currentState.events.data.filter { it.selected }.map { it.item })
    }
    sideEffect?.invoke()
    currentState.copyWithTransformedEvents { it.copy(selected = false) }
}
