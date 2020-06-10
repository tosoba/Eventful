package com.example.favourites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.model.event.IEvent
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.core.util.DataList
import com.example.core.util.LoadedSuccessfully
import com.example.coreandroid.base.BaseViewModel
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.coreandroid.util.*
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
) : BaseViewModel<FavouritesIntent, FavouritesState, FavouritesSignal>(
    savedStateHandle["initialState"] ?: FavouritesState()
) {

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<FavouritesViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): FavouritesViewModel
    }

    init {
        intents.onStart { emit(LoadFavourites) }
            .updates
            .applyToState(initialState = savedStateHandle["initialState"] ?: FavouritesState())
    }

    private val Flow<FavouritesIntent>.updates: Flow<Update>
        get() = merge(
            filterIsInstance<LoadFavourites>().loadFavouritesUpdates,
            filterIsInstance<EventLongClicked>().map { Update.ToggleEventSelection(it.event) },
            filterIsInstance<ClearSelectionClicked>().map { Update.ClearSelection },
            filterIsInstance<HideSnackbar>().map { Update.HideSnackbar },
            filterIsInstance<RemoveFromFavouritesClicked>().removeFromFavouritesUpdates
        )

    private val Flow<LoadFavourites>.loadFavouritesUpdates: Flow<Update>
        get() = filterNot { state.events.limitHit }
            .flatMapLatest {
                getSavedEventsFlow(state.limit + limitIncrement)
                    .flowOn(ioDispatcher)
                    .map { events -> Update.Events(events) }
            }

    private val Flow<RemoveFromFavouritesClicked>.removeFromFavouritesUpdates: Flow<Update>
        get() = map {
            val selectedEvents = state.events.data.filter { it.selected }.map { it.item }
            withContext(ioDispatcher) { deleteEvents(selectedEvents) }
            signal(FavouritesSignal.FavouritesRemoved)
            Update.RemovedFromFavourites(
                snackbarText = removedFromFavouritesMessage(eventsCount = selectedEvents.size),
                onSnackbarDismissed = { viewModelScope.launch { intent(HideSnackbar) } }
            )
        }

    private sealed class Update : StateUpdate<FavouritesState> {
        class ToggleEventSelection(
            override val event: Event
        ) : Update(),
            ToggleEventSelectionUpdate<FavouritesState>

        object ClearSelection : Update(), ClearSelectionUpdate<FavouritesState>

        object HideSnackbar : Update() {
            override fun invoke(state: FavouritesState): FavouritesState = state
                .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
        }

        class Events(private val events: List<IEvent>) : Update() {
            override fun invoke(state: FavouritesState): FavouritesState = state.copy(
                events = DataList(
                    data = events.map { Selectable(Event(it)) },
                    status = LoadedSuccessfully,
                    limitHit = state.events.data.size == events.size
                ),
                limit = events.size
            )
        }

        class RemovedFromFavourites(
            override val snackbarText: String,
            override val onSnackbarDismissed: () -> Unit
        ) : Update(),
            EventSelectionConfirmedUpdate<FavouritesState>
    }

    companion object {
        const val limitIncrement: Int = 20
    }
}
