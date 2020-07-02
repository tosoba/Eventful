package com.example.search

import com.example.core.usecase.SaveEvents
import com.example.core.util.PagedDataList
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.base.addedToFavouritesMessage
import com.example.test.rule.event
import com.example.test.rule.mockedList
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@FlowPreview
internal class OtherSearchFlowProcessorTests : BaseSearchFlowProcessorTests() {

    @Test
    @DisplayName("On AddToFavouritesClicked  - should saveEvents, signal events were saved and emit AddedToFavourites")
    fun addToFavouritesTest() = testScope.runBlockingTest {
        val saveEvents = mockk<SaveEvents>(relaxed = true)
        val selectableEvents = mockedList(20) { event(it) }
            .mapIndexed { index, event -> Selectable(event, index % 2 == 0) }
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState(
                events = PagedDataList(selectableEvents)
            )
        }

        abstract class Signal {
            abstract suspend operator fun invoke(signal: SearchSignal)
        }

        val signal = mockk<Signal>(relaxed = true)

        val updates = flowProcessor(saveEvents = saveEvents)
            .updates(
                intents = flowOf(SearchIntent.AddToFavouritesClicked),
                currentState = currentState,
                signal = signal::invoke
            )
            .toList()

        val selectedEvents = selectableEvents.filter { it.selected }.map { it.item }
        coVerify(exactly = 1) { saveEvents(selectedEvents) }
        coVerify(exactly = 1) { signal(SearchSignal.FavouritesSaved) }
        assert(updates.size == 1)
        val update = updates.first()
        assert(
            update is SearchStateUpdate.Events.AddedToFavourites
                    && update.snackbarText == addedToFavouritesMessage(
                eventsCount = selectedEvents.size
            )
        )
    }

    @Test
    @DisplayName("On EventLongClicked  - should emit Update.ToggleEventSelection")
    fun eventLongClickedTest() = testScope.runBlockingTest {
        val event = event()
        val updates = flowProcessor()
            .updates(intents = flowOf(SearchIntent.EventLongClicked(event)))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == SearchStateUpdate.ToggleEventSelection(event))
    }

    @Test
    @DisplayName("On ClearSelectionClicked - should emit Update.ClearSelection")
    fun clearSelectionTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(SearchIntent.ClearSelectionClicked))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == SearchStateUpdate.ClearSelection)
    }

    @Test
    @DisplayName("On HideSnackbar - should emit Update.HideSnackbar")
    fun hideSnackbarTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(SearchIntent.HideSnackbar))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == SearchStateUpdate.HideSnackbar)
    }
}