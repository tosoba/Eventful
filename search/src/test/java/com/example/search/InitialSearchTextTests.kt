package com.example.search

import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.usecase.GetSearchSuggestions
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class InitialSearchTextTests : BaseSearchFlowProcessorTests() {

    @Test
    @DisplayName("When initial text is not blank - should call getSearchSuggestions and getPagedEventsFlow")
    fun initialSearchTextTest() = testScope.runBlockingTest {
        val searchText = "test"
        val initialState = SearchState(searchText = searchText)
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk<Selectable<Event>>(any(), any(), any()) } returns emptyFlow()
        }
        val getSearchSuggestions = mockk<GetSearchSuggestions> {
            coEvery { this@mockk(any()) } returns emptyList()
        }

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            getSearchSuggestions = getSearchSuggestions
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).launchIn(testScope)

        coVerify(exactly = 1) { getSearchSuggestions(searchText) }
        coVerify(exactly = 1) { getPagedEventsFlow(initialState.items, any(), any()) }
    }

    @Test
    @DisplayName("When initial text not blank - should not call getSearchSuggestions and getPagedEventsFlow")
    fun noInitialSearchTextTest() = testScope.runBlockingTest {
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState()
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk<Selectable<Event>>(any(), any(), any()) } returns emptyFlow()
        }
        val getSearchSuggestions = mockk<GetSearchSuggestions> {
            coEvery { this@mockk(any()) } returns emptyList()
        }

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            getSearchSuggestions = getSearchSuggestions
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).launchIn(testScope)

        coVerify(exactly = 0) { getSearchSuggestions(any()) }
        coVerify(exactly = 0) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }
}