package com.example.favourites

import com.example.core.model.event.trimmedLowerCasedName
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.coreandroid.base.FlowProcessor
import com.example.coreandroid.util.removedFromFavouritesMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesFlowProcessor(
    private val getSavedEventsFlow: GetSavedEventsFlow,
    private val deleteEvents: DeleteEvents,
    private val ioDispatcher: CoroutineDispatcher,
    private val loadFavouritesOnStart: Boolean = true
) : FlowProcessor<FavouritesIntent, FavouritesStateUpdate, FavouritesState, FavouritesSignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<FavouritesIntent>,
        currentState: () -> FavouritesState,
        states: StateFlow<FavouritesState>,
        intent: suspend (FavouritesIntent) -> Unit,
        signal: suspend (FavouritesSignal) -> Unit
    ): Flow<FavouritesStateUpdate> = intents
        .run {
            if (loadFavouritesOnStart) onStart { emit(FavouritesIntent.LoadFavourites) }
            else this
        }
        .updates(coroutineScope, currentState, states, intent, signal)

    private fun Flow<FavouritesIntent>.updates(
        coroutineScope: CoroutineScope,
        currentState: () -> FavouritesState,
        states: StateFlow<FavouritesState>,
        intent: suspend (FavouritesIntent) -> Unit,
        signal: suspend (FavouritesSignal) -> Unit
    ): Flow<FavouritesStateUpdate> = merge(
        filterIsInstance<FavouritesIntent.NewSearch>()
            .map { (searchText) -> FavouritesStateUpdate.SearchTextUpdate(searchText) },
        filterIsInstance<FavouritesIntent.LoadFavourites>()
            .loadFavouritesUpdates(currentState, states),
        filterIsInstance<FavouritesIntent.EventLongClicked>()
            .map { (event) -> FavouritesStateUpdate.ToggleEventSelection(event) },
        filterIsInstance<FavouritesIntent.ClearSelectionClicked>()
            .map { FavouritesStateUpdate.ClearSelection },
        filterIsInstance<FavouritesIntent.HideSnackbar>()
            .map { FavouritesStateUpdate.HideSnackbar },
        filterIsInstance<FavouritesIntent.RemoveFromFavouritesClicked>()
            .removeFromFavouritesUpdates(coroutineScope, currentState, intent, signal)
    )

    private fun Flow<FavouritesIntent.LoadFavourites>.loadFavouritesUpdates(
        currentState: () -> FavouritesState,
        states: StateFlow<FavouritesState>
    ): Flow<FavouritesStateUpdate> = filterNot { currentState().events.limitHit }
        .flatMapLatest {
            getSavedEventsFlow(currentState().limit + limitIncrement)
                .flowOn(ioDispatcher)
                .combine(states.map { it.searchText }) { events, searchText -> events to searchText }
                .map { (events, searchText) ->
                    FavouritesStateUpdate.Events(
                        events = events.run {
                            if (searchText.isBlank()) this
                            else filter {
                                it.trimmedLowerCasedName.contains(
                                    searchText.toLowerCase(Locale.getDefault()).trim()
                                )
                            }
                        }
                    )
                }
        }

    private fun Flow<FavouritesIntent.RemoveFromFavouritesClicked>.removeFromFavouritesUpdates(
        coroutineScope: CoroutineScope,
        currentState: () -> FavouritesState,
        intent: suspend (FavouritesIntent) -> Unit,
        signal: suspend (FavouritesSignal) -> Unit
    ): Flow<FavouritesStateUpdate> = map {
        val selectedEvents = currentState().events.data.filter { it.selected }.map { it.item }
        withContext(ioDispatcher) { deleteEvents(selectedEvents) }
        signal(FavouritesSignal.FavouritesRemoved)
        FavouritesStateUpdate.RemovedFromFavourites(
            snackbarText = removedFromFavouritesMessage(eventsCount = selectedEvents.size),
            onSnackbarDismissed = {
                coroutineScope.launch { intent(FavouritesIntent.HideSnackbar) }
            }
        )
    }

    companion object {
        const val limitIncrement: Int = 20
    }
}
