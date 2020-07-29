package com.eventful.favourites

import com.eventful.core.usecase.event.GetSavedEventsFlow
import com.eventful.test.event
import com.eventful.test.mockedList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.jupiter.api.DisplayName

@FlowPreview
@ExperimentalCoroutinesApi
internal class LoadFavouritesTests : BaseFavouritesFlowProcessorTests() {

    @Test
    @DisplayName("On LoadFavourites - should call getSavedEventsFlow and emit Update.Events")
    fun loadFavouritesTest() = testScope.runBlockingTest {
        val events = mockedList(20) {
            event(it)
        }
        val getSavedEventsFlow = mockk<GetSavedEventsFlow> {
            every { this@mockk(any()) } returns flowOf(events)
        }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        val updates = flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
            .updates(
                intents = flowOf(FavouritesIntent.LoadFavourites),
                currentState = currentState
            )
            .toList()

        verify(exactly = 1) { getSavedEventsFlow(limit) }
        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.Events(events))
    }

    @Test
    @DisplayName("On LoadFavourites - should call getSavedEventsFlow and emit an Update.Events for each event flow emission")
    fun loadFavouritesMultipleEmissionsTest() = testScope.runBlockingTest {
        val events1stEmission = mockedList(20) {
            event(it)
        }
        val events2ndEmission = mockedList(25) {
            event(it)
        }
        val getSavedEventsFlow = mockk<GetSavedEventsFlow> {
            every { this@mockk(any()) } returns flowOf(events1stEmission, events2ndEmission)
        }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        val updates = flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
            .updates(
                intents = flowOf(FavouritesIntent.LoadFavourites),
                currentState = currentState
            )
            .toList()

        verify(exactly = 1) { getSavedEventsFlow(limit) }
        assert(updates.size == 2)
        assert(updates.first() == FavouritesStateUpdate.Events(events1stEmission))
        assert(updates.last() == FavouritesStateUpdate.Events(events2ndEmission))
    }

    @Test
    @DisplayName("When loadFavouritesOnStart is true - should call getSavedEventsFlow")
    fun loadingFavouritesOnStartTest() = testScope.runBlockingTest {
        val getSavedEventsFlow = mockk<GetSavedEventsFlow> {
            every { this@mockk(any()) } returns flowOf(
                mockedList(
                    20
                ) { event(it) })
        }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        flowProcessor(
            getSavedEventsFlow = getSavedEventsFlow,
            loadFavouritesOnStart = true
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).toList()

        verify(exactly = 1) { getSavedEventsFlow(limit) }
    }

    @Test
    @DisplayName("When loadFavouritesOnStart is false - should not call getSavedEventsFlow")
    fun noLoadingFavouritesOnStartTest() = testScope.runBlockingTest {
        val getSavedEventsFlow = mockk<GetSavedEventsFlow>(relaxed = true)
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        flowProcessor(
            getSavedEventsFlow = getSavedEventsFlow,
            loadFavouritesOnStart = false
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).toList()

        verify(exactly = 0) { getSavedEventsFlow(limit) }
    }
}