package com.example.favourites

import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.core.util.ext.lowerCasedTrimmed
import com.example.coreandroid.base.FlowProcessor
import com.example.coreandroid.base.removedFromFavouritesMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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
        states: Flow<FavouritesState>,
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
        states: Flow<FavouritesState>,
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
        states: Flow<FavouritesState>
    ): Flow<FavouritesStateUpdate> = filterNot { currentState().events.limitHit }
        .flatMapLatest { states.map { it.searchText }.onStart { emit(currentState().searchText) }.distinctUntilChanged() }
        .flatMapLatest { searchText ->
            getSavedEventsFlow(currentState().limit + limitIncrement)
                .flowOn(ioDispatcher)
                .map { events ->
                    FavouritesStateUpdate.Events(
                        events = events.run {
                            if (searchText.isBlank()) this
                            else filter {
                                it.name.lowerCasedTrimmed.contains(searchText.lowerCasedTrimmed)
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
            snackbarText = removedFromFavouritesMessage(
                eventsCount = selectedEvents.size
            ),
            onSnackbarDismissed = {
                coroutineScope.launch { intent(FavouritesIntent.HideSnackbar) }
            }
        )
    }

    companion object {
        const val limitIncrement: Int = 20
    }
}
