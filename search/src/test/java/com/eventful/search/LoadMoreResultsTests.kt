package com.eventful.search

import com.eventful.core.android.model.event.Event
import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.Selectable
import com.eventful.core.model.event.IEvent
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.util.Loading
import com.eventful.core.util.PagedDataList
import com.eventful.test.event
import com.eventful.test.mockedList
import com.eventful.test.relaxedMockedList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class LoadMoreResultsTests : BaseSearchFlowProcessorTests() {

    @Test
    @DisplayName("When events status is Loading - should not getPagedEvents")
    fun shouldNotLoadMoreWhenLoadingTest() =
        testScope.runBlockingTest {
            val currentState =
                mockk<() -> SearchState> {
                    every { this@mockk() } returns
                        SearchState(
                            items = PagedDataList(status = Loading, data = relaxedMockedList(10)))
                }
            val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

            flowProcessor(getPagedEvents = getPagedEvents)
                .updates(
                    currentState = currentState, intents = flowOf(SearchIntent.LoadMoreResults))
                .launchIn(this)

            coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
        }

    @Test
    @DisplayName("When no events - should not getPagedEvents")
    fun shouldNotLoadMoreWhenEventsEmptyTest() =
        testScope.runBlockingTest {
            val currentState =
                mockk<() -> SearchState> {
                    every { this@mockk() } returns SearchState(items = PagedDataList())
                }
            val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

            flowProcessor(getPagedEvents = getPagedEvents)
                .updates(
                    currentState = currentState, intents = flowOf(SearchIntent.LoadMoreResults))
                .launchIn(this)

            coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
        }

    @Test
    @DisplayName("When all events were loaded - should not getPagedEvents")
    fun shouldNotLoadMoreWhenAllEventsAreLoaded() =
        testScope.runBlockingTest {
            val currentState =
                mockk<() -> SearchState> {
                    every { this@mockk() } returns
                        SearchState(
                            items =
                                PagedDataList(data = relaxedMockedList(10), offset = 2, limit = 1))
                }

            val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

            flowProcessor(getPagedEvents = getPagedEvents)
                .updates(
                    currentState = currentState, intents = flowOf(SearchIntent.LoadMoreResults))
                .launchIn(this)

            coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
        }

    @Test
    @DisplayName("On load more - should emit loading and events loaded updates")
    fun loadMoreUpdatesTest() =
        testScope.runBlockingTest {
            val currentEvents = PagedDataList(data = mockedList(10) { Selectable(event(it)) })
            val currentState =
                mockk<() -> SearchState> {
                    every { this@mockk() } returns SearchState(items = currentEvents)
                }
            val expectedResource =
                Resource.successWith(PagedResult<IEvent>(mockedList(10) { event(it) }, 1, 1))
            val getPagedEvents =
                mockk<GetPagedEvents> {
                    coEvery { this@mockk<Selectable<Event>>(any(), any(), any()) } returns
                        expectedResource
                }

            val updates =
                flowProcessor(getPagedEvents = getPagedEvents)
                    .updates(
                        intents = flowOf(SearchIntent.LoadMoreResults), currentState = currentState)
                    .toList()

            coVerify(exactly = 1) { getPagedEvents(currentEvents, any(), any()) }
            val loadingUpdate = updates.first()
            assert(
                loadingUpdate is SearchStateUpdate.Events.Loading &&
                    loadingUpdate.searchText == null)
            val eventsUpdate = updates.last()
            assert(
                eventsUpdate is SearchStateUpdate.Events.Loaded &&
                    eventsUpdate.resource == expectedResource &&
                    !eventsUpdate.newSearch)
        }
}
