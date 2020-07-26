package com.example.favourites

import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.core.util.DataList
import com.example.coreandroid.base.removedFromFavouritesMessage
import com.example.coreandroid.model.event.Selectable
import com.example.test.rule.event
import com.example.test.rule.mockedList
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.jupiter.api.DisplayName

@ExperimentalCoroutinesApi
@FlowPreview
internal class OtherFavouritesFlowProcessorTests : BaseFavouritesFlowProcessorTests() {

    @Test
    @DisplayName(
        """On RemoveFromFavouritesClicked 
|- should delete selected events, signal FavouritesRemoved and show removedFromFavouritesMessage"""
    )
    fun removeFromFavouritesTest() = testScope.runBlockingTest {
        val deleteEvents = mockk<DeleteEvents>(relaxed = true)
        val selectableEvents = mockedList(20) { event(it) }
            .mapIndexed { index, event -> Selectable(event, index % 2 == 0) }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState(
                items = DataList(selectableEvents)
            )
        }

        abstract class Signal {
            abstract suspend operator fun invoke(signal: FavouritesSignal)
        }

        val signal = mockk<Signal>(relaxed = true)

        val updates = flowProcessor(deleteEvents = deleteEvents)
            .updates(
                intents = flowOf(FavouritesIntent.RemoveFromFavouritesClicked),
                currentState = currentState,
                signal = signal::invoke
            )
            .toList()

        val selectedEvents = selectableEvents.filter { it.selected }.map { it.item }
        verify(exactly = 1) { currentState() }
        coVerify(exactly = 1) { deleteEvents(selectedEvents) }
        coVerify(exactly = 1) { signal(FavouritesSignal.FavouritesRemoved) }
        assert(updates.size == 1)
        val update = updates.first()
        assert(
            update is FavouritesStateUpdate.RemovedFromFavourites
                    && update.snackbarText == removedFromFavouritesMessage(
                eventsCount = selectedEvents.size
            )
        )
    }

    @Test
    @DisplayName("On LoadFavourites when limit was hit - should not call getSavedEventsFlow")
    fun loadFavouritesLimitHitTest() = testScope.runBlockingTest {
        val getSavedEventsFlow = mockk<GetSavedEventsFlow>(relaxed = true)
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState(items = DataList(limitHit = true))
        }

        flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
            .updates(
                intents = flowOf(FavouritesIntent.LoadFavourites),
                currentState = currentState
            )
            .launchIn(testScope)

        verify(exactly = 1) { currentState() }
        verify(exactly = 0) { getSavedEventsFlow(any()) }
    }

    @Test
    @DisplayName("On EventLongClicked  - should emit Update.ToggleEventSelection")
    fun eventLongClickedTest() = testScope.runBlockingTest {
        val event = event()

        val updates = flowProcessor()
            .updates(intents = flowOf(FavouritesIntent.EventLongClicked(event)))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.ToggleEventSelection(event))
    }

    @Test
    @DisplayName("On ClearSelectionClicked - should emit Update.ClearSelection")
    fun clearSelectionTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(FavouritesIntent.ClearSelectionClicked))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.ClearSelection)
    }

    @Test
    @DisplayName("On HideSnackbar - should emit Update.HideSnackbar")
    fun hideSnackbarTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(FavouritesIntent.HideSnackbar))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.HideSnackbar)
    }
}
