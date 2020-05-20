package com.example.coreandroid.util

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.model.ticketmaster.trimmedLowerCasedName
import com.example.core.usecase.SaveEvents
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SelectableEventsState<S : SelectableEventsState<S>> {
    val events: HoldsList<Selectable<Event>>
    fun copyWithTransformedEvents(transform: (Selectable<Event>) -> Selectable<Event>): S
}

interface ClearEventSelectionIntent

fun <I : ClearEventSelectionIntent, S : SelectableEventsState<S>> Flow<Pair<I, S>>.processClearSelectionIntents(): Flow<S> {
    return map { (_, state) -> state.copyWithTransformedEvents { it.copy(selected = false) } }
}

interface EventSelectionToggledIntent {
    val event: Event
}

fun <I : EventSelectionToggledIntent, S : SelectableEventsState<S>> Flow<Pair<I, S>>.processEventLongClickedIntents(): Flow<S> {
    return map { (intent, state) ->
        state.copyWithTransformedEvents {
            if (it.item.id == intent.event.id) Selectable(intent.event, !it.selected) else it
        }
    }
}

interface SelectableEventsSnackbarState<S : SelectableEventsSnackbarState<S>> :
    SelectableEventsState<S>, HoldsSnackbarState<S> {
    fun copyWithSnackbarStateAndTransformedEvents(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): S
}

interface HoldsSnackbarState<S> {
    fun copyWithSnackbarState(snackbarState: SnackbarState): S
}

interface HideSnackbarIntent

fun <I : HideSnackbarIntent, S : HoldsSnackbarState<S>> Flow<Pair<I, S>>.processHideSnackbarIntents(): Flow<S> {
    return map { (_, state) -> state.copyWithSnackbarState(snackbarState = SnackbarState.Hidden) }
}

interface AddToFavouritesIntent

fun <I : AddToFavouritesIntent, S : SelectableEventsSnackbarState<S>> Flow<Pair<I, S>>.processAddToFavouritesIntentsWithSnackbar(
    saveEvents: SaveEvents,
    ioDispatcher: CoroutineDispatcher,
    onDismissed: (() -> Unit)? = null,
    sideEffect: (() -> Unit)? = null
): Flow<S> = map { (_, currentState) ->
    val selectedEvents = currentState.events.data.filter { it.selected }.map { it.item }
    withContext(ioDispatcher) { saveEvents(selectedEvents) }
    sideEffect?.invoke()
    currentState.copyWithSnackbarStateAndTransformedEvents(
        SnackbarState.Shown(
            """${selectedEvents.size}
                |${if (selectedEvents.size > 1) " events were" else " event was"} 
                |added to favourites""".trimMargin().replace("\n", ""),
            length = Snackbar.LENGTH_SHORT,
            onDismissed = onDismissed
        )
    ) { event ->
        event.copy(selected = false)
    }
}

fun <T> PagedDataList<T>.followingEventsFlow(
    dispatcher: CoroutineDispatcher,
    toEvent: (T) -> IEvent,
    getEvents: suspend (Int) -> Resource<PagedResult<IEvent>>
): Flow<Resource<PagedResult<IEvent>>> = flow {
    var page = offset
    var resource: Resource<PagedResult<IEvent>>
    do {
        resource = withContext(dispatcher) { getEvents(page++) }
    } while (resource is Resource.Success<PagedResult<IEvent>>
        && (data.map(toEvent) + resource.data.items)
            .distinctBy { it.trimmedLowerCasedName }.size == data.size
        && page < limit
    )
    emit(resource)
}

interface LoadMoreEventsIntent

fun <I : LoadMoreEventsIntent, S : SelectableEventsState<S>> Flow<Pair<I, S>>.filterCanLoadMoreEvents(): Flow<Pair<I, S>> {
    return filterNot { (_, currentState) ->
        val events = currentState.events
        events.status is Loading || !events.canLoadMore || events.data.isEmpty()
    }
}
