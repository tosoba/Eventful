package com.eventful.search

import com.eventful.core.android.model.event.Event
import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.Selectable
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.usecase.search.GetSearchSuggestions
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
    @DisplayName("When initial text is not blank - should call getSearchSuggestions and getPagedEvents")
    fun initialSearchTextTest() = testScope.runBlockingTest {
        val searchText = "test"
        val initialState = SearchState(searchText = searchText)
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEvents = mockk<GetPagedEvents> {
            coEvery {
                this@mockk<Selectable<Event>>(any(), any(), any())
            } returns Resource.successWith(
                PagedResult(emptyList(), 0, 0)
            )
        }
        val getSearchSuggestions = mockk<GetSearchSuggestions> {
            coEvery { this@mockk(any()) } returns emptyList()
        }

        flowProcessor(
            getPagedEvents = getPagedEvents,
            getSearchSuggestions = getSearchSuggestions
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).launchIn(testScope)

        coVerify(exactly = 1) { getSearchSuggestions(searchText) }
        coVerify(exactly = 1) { getPagedEvents(initialState.items, any(), any()) }
    }

    @Test
    @DisplayName("When initial text not blank - should not call getSearchSuggestions and getPagedEvents")
    fun noInitialSearchTextTest() = testScope.runBlockingTest {
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState()
        }
        val getPagedEvents = mockk<GetPagedEvents> {
            coEvery {
                this@mockk<Selectable<Event>>(any(), any(), any())
            } returns Resource.successWith(
                PagedResult(emptyList(), 0, 0)
            )
        }
        val getSearchSuggestions = mockk<GetSearchSuggestions> {
            coEvery { this@mockk(any()) } returns emptyList()
        }

        flowProcessor(
            getPagedEvents = getPagedEvents,
            getSearchSuggestions = getSearchSuggestions
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).launchIn(testScope)

        coVerify(exactly = 0) { getSearchSuggestions(any()) }
        coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
    }
}