package com.eventful.nearby

import com.eventful.core.usecase.event.SaveEvents
import com.eventful.core.util.PagedDataList
import com.eventful.core.android.base.addedToFavouritesMessage
import com.eventful.core.model.Selectable
import com.eventful.test.event
import com.eventful.test.mockedList
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

@FlowPreview
@ExperimentalCoroutinesApi
internal class OtherNearbyFlowProcessorTests : BaseNearbyFlowProcessorTests() {
    @Test
    @DisplayName("On AddToFavouritesClicked  - should saveEvents, signal events were saved and emit AddedToFavourites")
    fun addToFavouritesTest() = testScope.runBlockingTest {
        val saveEvents = mockk<SaveEvents>(relaxed = true)
        val selectableEvents = mockedList(20) { event(it) }
            .mapIndexed { index, event -> Selectable(event, index % 2 == 0) }
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns NearbyState(
                items = PagedDataList(selectableEvents)
            )
        }
        val signal = mockk<Signal>(relaxed = true)

        val updates = flowProcessor(saveEvents = saveEvents)
            .updates(
                intents = flowOf(NearbyIntent.AddToFavouritesClicked),
                currentState = currentState,
                signal = signal::invoke
            )
            .toList()

        val selectedEvents = selectableEvents.filter { it.selected }.map { it.item }
        coVerify(exactly = 1) { saveEvents(selectedEvents) }
        coVerify(exactly = 1) { signal(NearbySignal.FavouritesSaved) }
        assert(updates.size == 1)
        val update = updates.first()
        assert(
            update is NearbyStateUpdate.Events.AddedToFavourites
                    && update.snackbarText == addedToFavouritesMessage(eventsCount = selectedEvents.size)
        )
    }


    @Test
    @DisplayName("On EventLongClicked  - should emit Update.ToggleEventSelection")
    fun eventLongClickedTest() = testScope.runBlockingTest {
        val event = event()
        val updates = flowProcessor()
            .updates(intents = flowOf(NearbyIntent.EventLongClicked(event)))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == NearbyStateUpdate.ToggleEventSelection(event))
    }

    @Test
    @DisplayName("On ClearSelectionClicked - should emit Update.ClearSelection")
    fun clearSelectionTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(NearbyIntent.ClearSelectionClicked))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == NearbyStateUpdate.ClearSelection)
    }

    @Test
    @DisplayName("On HideSnackbar - should emit Update.HideSnackbar")
    fun hideSnackbarTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(NearbyIntent.HideSnackbar))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == NearbyStateUpdate.HideSnackbar)
    }
}