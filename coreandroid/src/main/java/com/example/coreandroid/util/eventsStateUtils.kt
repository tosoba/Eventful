package com.example.coreandroid.util

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.model.ticketmaster.trimmedLowerCasedName
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface SelectableEventsState<S : SelectableEventsState<S>> {
    val events: HoldsList<Selectable<Event>>
    fun copyWithTransformedEvents(transform: (Selectable<Event>) -> Selectable<Event>): S
}

interface SelectableEventsSnackbarState<S : SelectableEventsSnackbarState<S>> :
    SelectableEventsState<S>,
    HoldsSnackbarState<S> {
    fun copyWithSnackbarStateAndTransformedEvents(
        snackbarState: SnackbarState,
        transform: (Selectable<Event>) -> Selectable<Event>
    ): S
}

interface HoldsSnackbarState<S> {
    fun copyWithSnackbarState(snackbarState: SnackbarState): S
}

fun <Event> pagedEventsFlow(
    currentEvents: PagedDataList<Event>,
    dispatcher: CoroutineDispatcher,
    toEvent: (Event) -> IEvent,
    getEvents: suspend (Int) -> Resource<PagedResult<IEvent>>
): Flow<Resource<PagedResult<IEvent>>> = flow {
    var page = currentEvents.offset
    var resource: Resource<PagedResult<IEvent>>
    do {
        //TODO: do distinct filtering here
        resource = withContext(dispatcher) { getEvents(page++) }
    } while (resource is Resource.Success<PagedResult<IEvent>>
        && (currentEvents.data.map(toEvent) + resource.data.items)
            .distinctBy { it.trimmedLowerCasedName }.size == currentEvents.data.size
        && page < currentEvents.limit
    )
    emit(resource)
}

interface StateUpdate<State : Any> {
    operator fun invoke(state: State): State
}

interface ClearSelectionUpdate<S : SelectableEventsState<S>> : StateUpdate<S> {
    override fun invoke(state: S): S = state.copyWithTransformedEvents { it.copy(selected = false) }
}

interface ToggleEventSelectionUpdate<S : SelectableEventsState<S>> :
    StateUpdate<S> {
    val event: Event
    override fun invoke(state: S): S = state.copyWithTransformedEvents {
        if (it.item.id == event.id) Selectable(event, !it.selected) else it
    }
}

interface EventSelectionConfirmedUpdate<S : SelectableEventsSnackbarState<S>> : StateUpdate<S> {
    val snackbarText: String
    val onSnackbarDismissed: () -> Unit
    override fun invoke(state: S): S = state.copyWithSnackbarStateAndTransformedEvents(
        snackbarState = SnackbarState.Shown(
            text = snackbarText,
            length = Snackbar.LENGTH_SHORT,
            onDismissed = onSnackbarDismissed
        )
    ) { event -> event.copy(selected = false) }
}

fun addedToFavouritesMessage(eventsCount: Int): String = """$eventsCount
            |${if (eventsCount > 1) " events were" else " event was"} 
            |added to favourites""".trimMargin().replace("\n", "")

fun removedFromFavouritesMessage(eventsCount: Int): String = """$eventsCount
            |${if (eventsCount > 1) " events were" else " event was"} 
            |added to favourites""".trimMargin().replace("\n", "")
