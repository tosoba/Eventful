package com.example.favourites

import androidx.lifecycle.viewModelScope
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.base.BaseStateFlowViewModel
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.DataList
import com.example.coreandroid.util.LoadedSuccessfully
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

@FlowPreview
@ExperimentalCoroutinesApi
class FavouritesViewModel(
    private val getSavedEvents: GetSavedEvents,
    private val deleteEvents: DeleteEvents,
    private val ioDispatcher: CoroutineDispatcher,
    initialState: FavouritesState = FavouritesState()
) : BaseStateFlowViewModel<FavouritesIntent, FavouritesState, FavouritesSignal>(initialState) {

    init {
        intents.onStart { emit(LoadFavourites) }
            .onEach { intent ->
                if (intent is RemoveFromFavouritesClicked) withContext(ioDispatcher) {
                    deleteEvents(state.events.data.filter { it.selected }.map { it.item })
                }
                signal(FavouritesSignal.FavouritesRemoved)
            }
            .updates()
            .scan(initialState) { state, update -> update(state) }
            .onEach { state = it }
            .launchIn(viewModelScope)
    }

    private fun Flow<FavouritesIntent>.updates(): Flow<Update> = merge(
        filterIsInstance<LoadFavourites>().loadFavouritesUpdates,
        filterIsInstance<EventLongClicked>().map { Update.ToggleEventSelection(it.event) },
        filterIsInstance<ClearSelectionClicked>().map { Update.ClearSelection }
    )

    private val Flow<LoadFavourites>.loadFavouritesUpdates: Flow<Update>
        get() = filterNot { state.events.limitHit }
            .flatMapLatest {
                getSavedEvents(state.limit + limitIncrement)
                    .flowOn(ioDispatcher)
                    .map { events -> Update.Events(events) }
            }

    private sealed class Update : StateUpdate<FavouritesState> {
        class ToggleEventSelection(private val event: Event) : Update() {
            override operator fun invoke(state: FavouritesState): FavouritesState = state
                .copyWithTransformedEvents {
                    if (it.item.id == event.id) Selectable(event, !it.selected) else it
                }
        }

        object ClearSelection : Update() {
            override operator fun invoke(state: FavouritesState): FavouritesState = state
                .copyWithTransformedEvents { it.copy(selected = false) }
        }

        class Events(val events: List<IEvent>) : Update() {
            override operator fun invoke(state: FavouritesState): FavouritesState = state.copy(
                events = DataList(
                    data = events.map { Selectable(Event(it)) },
                    status = LoadedSuccessfully,
                    limitHit = state.events.data.size == events.size
                ),
                limit = events.size
            )
        }
    }

    companion object {
        const val limitIncrement: Int = 20
    }
}
