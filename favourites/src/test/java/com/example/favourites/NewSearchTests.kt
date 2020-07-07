package com.example.favourites

import com.example.core.usecase.GetSavedEventsFlow
import com.example.core.util.ext.lowerCasedTrimmed
import com.example.test.rule.event
import com.example.test.rule.mockedList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.jupiter.api.DisplayName

@FlowPreview
@ExperimentalCoroutinesApi
internal class NewSearchTests : BaseFavouritesFlowProcessorTests() {
    @Test
    @DisplayName("On NewSearch - should emit SearchText")
    fun newSearchTest() = testScope.runBlockingTest {
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val searchText = "test"

        val updates = flowProcessor()
            .updates(
                intents = flowOf(FavouritesIntent.NewSearch(searchText)),
                currentState = currentState
            )
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.SearchText(searchText))
    }

    @Test
    @DisplayName("After NewSearch update is applied to state - should emit Update.Events with filtered events")
    fun newSearchLoadFavouritesTest() = testScope.runBlockingTest {
        val events = mockedList(20) { event(it) }
        val getSavedEventsFlow = mockk<GetSavedEventsFlow> {
            every { this@mockk(any()) } returns flowOf(events)
        }
        val initialState = FavouritesState()
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns initialState
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement
        val searchText = "7"
        val searchTextUpdate = FavouritesStateUpdate.SearchText(searchText)

        val updates = flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
            .updates(
                intents = flowOf(
                    FavouritesIntent.LoadFavourites,
                    FavouritesIntent.NewSearch(searchText)
                ),
                currentState = currentState,
                states = flowOf(searchTextUpdate(initialState))
            )
            .toList()

        verify(exactly = 2) { getSavedEventsFlow(limit) }
        assert(updates.size == 3)
        assert(updates.first() == searchTextUpdate)
        val unfilteredEventsUpdate = updates[1]
        assert(
            unfilteredEventsUpdate is FavouritesStateUpdate.Events
                    && unfilteredEventsUpdate.events == events
        )
        val filteredEventsUpdate = updates.last()
        assert(
            filteredEventsUpdate is FavouritesStateUpdate.Events
                    && filteredEventsUpdate.events == events.filter {
                it.name.lowerCasedTrimmed.contains(searchText.lowerCasedTrimmed)
            }
        )
    }
}