package com.example.favourites

import androidx.lifecycle.SavedStateHandle
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.coreandroid.base.FlowProcessor
import com.example.coreandroid.util.removedFromFavouritesMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesFlowProcessor @Inject constructor(
    private val getSavedEventsFlow: GetSavedEventsFlow,
    private val deleteEvents: DeleteEvents,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<FavouritesIntent, FavouritesStateUpdate, FavouritesState, FavouritesSignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<FavouritesIntent>,
        currentState: () -> FavouritesState,
        intent: suspend (FavouritesIntent) -> Unit,
        signal: suspend (FavouritesSignal) -> Unit,
        savedStateHandle: SavedStateHandle
    ): Flow<FavouritesStateUpdate> = intents.onStart { emit(FavouritesIntent.LoadFavourites) }
        .updates(coroutineScope, currentState, intent, signal)

    private fun Flow<FavouritesIntent>.updates(
        coroutineScope: CoroutineScope,
        currentState: () -> FavouritesState,
        intent: suspend (FavouritesIntent) -> Unit,
        signal: suspend (FavouritesSignal) -> Unit
    ): Flow<FavouritesStateUpdate> = merge(
        filterIsInstance<FavouritesIntent.LoadFavourites>()
            .loadFavouritesUpdates(currentState),
        filterIsInstance<FavouritesIntent.EventLongClicked>()
            .map { FavouritesStateUpdate.ToggleEventSelection(it.event) },
        filterIsInstance<FavouritesIntent.ClearSelectionClicked>()
            .map { FavouritesStateUpdate.ClearSelection },
        filterIsInstance<FavouritesIntent.HideSnackbar>()
            .map { FavouritesStateUpdate.HideSnackbar },
        filterIsInstance<FavouritesIntent.RemoveFromFavouritesClicked>()
            .removeFromFavouritesUpdates(coroutineScope, currentState, intent, signal)
    )

    private fun Flow<FavouritesIntent.LoadFavourites>.loadFavouritesUpdates(
        currentState: () -> FavouritesState
    ): Flow<FavouritesStateUpdate> = filterNot { currentState().events.limitHit }
        .flatMapLatest {
            getSavedEventsFlow(currentState().limit + limitIncrement)
                .flowOn(ioDispatcher)
                .map { events -> FavouritesStateUpdate.Events(events) }
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
