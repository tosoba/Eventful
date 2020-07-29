package com.eventful.search

import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.event.IEvent
import com.eventful.core.model.search.SearchSuggestion
import com.eventful.core.usecase.event.GetPagedEventsFlow
import com.eventful.core.usecase.search.GetSearchSuggestions
import com.eventful.core.usecase.search.SaveSearchSuggestion
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable
import com.eventful.test.rule.event
import com.eventful.test.rule.mockedList
import com.eventful.test.rule.relaxedMockedList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class NewSearchTests : BaseSearchFlowProcessorTests() {

    @Test
    @DisplayName("When new search is confirmed - should saveSearchSuggestion")
    fun newSearchShouldSaveTest() = testScope.runBlockingTest {
        val saveSearchSuggestion = mockk<SaveSearchSuggestion>(relaxed = true)
        val searchText = "test"

        flowProcessorForShouldSaveTest(saveSearchSuggestion = saveSearchSuggestion)
            .updates(
                intents = flowOf(SearchIntent.NewSearch(searchText, true))
            )
            .launchIn(testScope)

        coVerify(exactly = 1) { saveSearchSuggestion(searchText) }
    }

    @Test
    @DisplayName("When new search is not confirmed - should not saveSearchSuggestion")
    fun newSearchShouldNotSaveTest() = testScope.runBlockingTest {
        val saveSearchSuggestion = mockk<SaveSearchSuggestion>(relaxed = true)
        val searchText = "test"

        flowProcessorForShouldSaveTest(saveSearchSuggestion = saveSearchSuggestion)
            .updates(
                intents = flowOf(SearchIntent.NewSearch(searchText, false))
            )
            .launchIn(testScope)

        coVerify(exactly = 0) { saveSearchSuggestion(searchText) }
    }

    private fun flowProcessorForShouldSaveTest(
        saveSearchSuggestion: SaveSearchSuggestion
    ) = flowProcessor(
        searchEvents = mockk {
            coEvery { this@mockk(any(), any()) } returns Resource.successWith(
                PagedResult(relaxedMockedList<IEvent>(20), 1, 1)
            )
        },
        getSearchSuggestions = mockk {
            coEvery { this@mockk(any()) } returns emptyList<SearchSuggestion>()
        },
        saveSearchSuggestion = saveSearchSuggestion
    )

    @Test
    @DisplayName("When given more than 1 equal searches - should getSearchSuggestions and getPagedEventsFlow only once")
    fun distinctNewSearchTest() = testScope.runBlockingTest {
        val searchText = "test"
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
            intents = (1..2).map { SearchIntent.NewSearch(searchText, false) }.asFlow(),
            currentState = currentState
        ).launchIn(testScope)

        coVerify(exactly = 1) { getSearchSuggestions(searchText) }
        coVerify(exactly = 1) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("On new search - should emit loading, suggestions and events loaded updates")
    fun newSearchUpdatesTest() = testScope.runBlockingTest {
        val searchText = "test"
        val initialState = SearchState()
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns initialState
        }
        val expectedResource = Resource.successWith(
            PagedResult<IEvent>(mockedList(10) { event(it) }, 1, 1)
        )
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk(initialState.items, any(), any()) } returns flowOf(
                expectedResource
            )
        }
        val expectedSuggestions = mockedList(10) {
            SearchSuggestion(it, "suggestion$it", 100L)
        }
        val getSearchSuggestions = mockk<GetSearchSuggestions> {
            coEvery { this@mockk(any()) } returns expectedSuggestions
        }

        val updates = flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            getSearchSuggestions = getSearchSuggestions
        ).updates(
            intents = flowOf(SearchIntent.NewSearch(searchText, true)),
            currentState = currentState
        ).toList()

        val loadingUpdate = updates.first()
        assert(
            loadingUpdate is SearchStateUpdate.Events.Loading
                    && loadingUpdate.searchText == searchText
        )
        val suggestionsUpdate = updates[1]
        assert(
            suggestionsUpdate is SearchStateUpdate.Suggestions
                    && suggestionsUpdate.suggestions == expectedSuggestions
        )
        val eventsUpdate = updates.last()
        assert(
            eventsUpdate is SearchStateUpdate.Events.Loaded
                    && eventsUpdate.resource == expectedResource
                    && eventsUpdate.newSearch
        )
    }
}