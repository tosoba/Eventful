package com.example.favourites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.util.removedFromFavouritesMessage
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesViewModel @AssistedInject constructor(
    private val getSavedEventsFlow: GetSavedEventsFlow,
    private val deleteEvents: DeleteEvents,
    private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel<FavouritesIntent, FavouritesStateUpdate, FavouritesState, FavouritesSignal>(
    savedStateHandle["initialState"] ?: FavouritesState()
) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<FavouritesViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): FavouritesViewModel
    }

    init {
        start()
    }

    override val updates: Flow<FavouritesStateUpdate>
        get() = intents.onStart { emit(FavouritesIntent.LoadFavourites) }.updates

    private val Flow<FavouritesIntent>.updates: Flow<FavouritesStateUpdate>
        get() = merge(
            filterIsInstance<FavouritesIntent.LoadFavourites>().loadFavouritesUpdates,
            filterIsInstance<FavouritesIntent.EventLongClicked>()
                .map { FavouritesStateUpdate.ToggleEventSelection(it.event) },
            filterIsInstance<FavouritesIntent.ClearSelectionClicked>()
                .map { FavouritesStateUpdate.ClearSelection },
            filterIsInstance<FavouritesIntent.HideSnackbar>()
                .map { FavouritesStateUpdate.HideSnackbar },
            filterIsInstance<FavouritesIntent.RemoveFromFavouritesClicked>()
                .removeFromFavouritesUpdates
        )

    private val Flow<FavouritesIntent.LoadFavourites>.loadFavouritesUpdates: Flow<FavouritesStateUpdate>
        get() = filterNot { state.events.limitHit }
            .flatMapLatest {
                getSavedEventsFlow(state.limit + limitIncrement)
                    .flowOn(ioDispatcher)
                    .map { events -> FavouritesStateUpdate.Events(events) }
            }

    private val Flow<FavouritesIntent.RemoveFromFavouritesClicked>.removeFromFavouritesUpdates: Flow<FavouritesStateUpdate>
        get() = map {
            val selectedEvents = state.events.data.filter { it.selected }.map { it.item }
            withContext(ioDispatcher) { deleteEvents(selectedEvents) }
            signal(FavouritesSignal.FavouritesRemoved)
            FavouritesStateUpdate.RemovedFromFavourites(
                snackbarText = removedFromFavouritesMessage(eventsCount = selectedEvents.size),
                onSnackbarDismissed = {
                    viewModelScope.launch { intent(FavouritesIntent.HideSnackbar) }
                }
            )
        }

    companion object {
        const val limitIncrement: Int = 20
    }
}
