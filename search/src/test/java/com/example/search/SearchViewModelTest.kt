package com.example.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveEvents
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.coreandroid.provider.ConnectivityStateProvider
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.takeWhileInclusive
import com.example.test.rule.onPausedDispatcher
import com.example.test.rule.relaxedMockedList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
internal class SearchViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
    }

    private fun searchViewModel(
        searchEvents: SearchEvents = mockk(relaxed = true),
        saveEvents: SaveEvents = mockk(relaxed = true),
        getSearchSuggestions: GetSeachSuggestions = mockk(relaxed = true),
        saveSuggestion: SaveSuggestion = mockk(relaxed = true),
        connectivityStateProvider: ConnectivityStateProvider = mockk(relaxed = true),
        initialState: SearchState = SearchState()
    ): SearchViewModel = SearchViewModel(
        searchEvents = searchEvents,
        saveEvents = saveEvents,
        getSearchSuggestions = getSearchSuggestions,
        saveSuggestion = saveSuggestion,
        connectivityStateProvider = connectivityStateProvider,
        ioDispatcher = testDispatcher,
        initialState = initialState
    )

    @Test
    fun `GivenSearchVM WhenConfirmedNewSearchIsSent SearchIsPerformedWithSavingNewSuggestion`() {
        testScope.runBlockingTest {
            val searchEvents = mockk<SearchEvents> {
                coEvery { this@mockk(any()) } returns Resource.successWith(
                    PagedResult(relaxedMockedList<IEvent>(20), 1, 1)
                )
            }
            val saveSuggestion = mockk<SaveSuggestion>(relaxed = true)
            val getSearchSuggestions = mockk<GetSeachSuggestions> {
                coEvery { this@mockk(any()) } returns emptyList<SearchSuggestion>()
            }
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                saveSuggestion = saveSuggestion,
                getSearchSuggestions = getSearchSuggestions
            )
            val searchText = "test"

            viewModel.send(NewSearch(searchText, true))

            coVerify(exactly = 1) { saveSuggestion(searchText) }
            coVerify(exactly = 1) { searchEvents(searchText) }
            coVerify(exactly = 1) { getSearchSuggestions(searchText) }
        }
    }

    @Test
    fun `GivenSearchVM WhenUnconfirmedNewSearchIsSent SearchIsPerformedWithoutSavingNewSuggestion`() {
        testScope.runBlockingTest {
            val searchEvents = mockk<SearchEvents> {
                coEvery { this@mockk(any()) } returns Resource.successWith(
                    PagedResult(relaxedMockedList<IEvent>(20), 1, 1)
                )
            }
            val saveSuggestion = mockk<SaveSuggestion>(relaxed = true)
            val getSearchSuggestions = mockk<GetSeachSuggestions> {
                coEvery { this@mockk(any()) } returns emptyList<SearchSuggestion>()
            }
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                saveSuggestion = saveSuggestion,
                getSearchSuggestions = getSearchSuggestions
            )
            val searchText = "test"

            viewModel.send(NewSearch(searchText, false))

            coVerify(exactly = 0) { saveSuggestion(searchText) }
            coVerify(exactly = 1) { searchEvents(searchText) }
            coVerify(exactly = 1) { getSearchSuggestions(searchText) }
        }
    }

    @Test
    fun `GivenSearchVM WhenSearchEventsReturnsSuccessfully ProperStateTransitionsOccur`() {
        testScope.runBlockingTest {
            val returnedEventsListSize = 20
            val currentPage = 0
            val totalPages = 1
            val searchEvents = mockk<SearchEvents> {
                coEvery { this@mockk(any()) } returns Resource.successWith(
                    PagedResult(
                        relaxedMockedList<IEvent>(returnedEventsListSize),
                        currentPage,
                        totalPages
                    )
                )
            }
            val returnedSuggestionsListSize = 20
            val getSearchSuggestions = mockk<GetSeachSuggestions> {
                coEvery { this@mockk(any()) } returns relaxedMockedList<SearchSuggestion>(
                    returnedSuggestionsListSize
                )
            }
            val initialState = SearchState()
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                getSearchSuggestions = getSearchSuggestions,
                initialState = initialState
            )
            val searchText = "test"

            val states = onPausedDispatcher {
                viewModel.send(NewSearch(searchText, false))
                viewModel.states.takeWhileInclusive { it.events.status !is LoadedSuccessfully }
                    .toList()
            }

            assert(states.size == 3)

            val (
                initialSearchText,
                initialSuggestions,
                initialEvents,
                initialSnackbarState
            ) = states.first()
            assert(
                initialSearchText == initialState.searchText
                        && initialSuggestions == initialState.searchSuggestions
                        && initialEvents == initialState.events
                        && initialSnackbarState == initialState.snackbarState
            )

            val loadingState = states[1]
            assert(
                loadingState.searchText == initialState.searchText
                        && loadingState.searchSuggestions == initialState.searchSuggestions
                        && loadingState.snackbarState == initialState.snackbarState
            )
            assert(
                loadingState.events.data.isEmpty()
                        && loadingState.events.status is Loading
                        && loadingState.events.offset == initialState.events.offset
                        && loadingState.events.limit == initialState.events.limit
            )

            val loadedState = states.last()
            assert(
                loadedState.searchText == searchText
                        && loadedState.searchSuggestions.size == returnedSuggestionsListSize
                        && loadedState.snackbarState == initialState.snackbarState
            )
            assert(
                loadedState.events.data.size == returnedEventsListSize
                        && loadedState.events.status is LoadedSuccessfully
                        && loadedState.events.offset == currentPage + 1
                        && loadedState.events.limit == totalPages
            )
        }
    }

    @Test
    fun `GivenSearchVM WhenTwoEqualNewSearchesAreSent OnlyFirstIsProcessed`() {
        testScope.runBlockingTest {
            val searchEvents = mockk<SearchEvents> {
                coEvery { this@mockk(any()) } returns Resource.successWith(
                    PagedResult(relaxedMockedList<IEvent>(20), 0, 1)
                )
            }
            val getSearchSuggestions = mockk<GetSeachSuggestions> {
                coEvery { this@mockk(any()) } returns relaxedMockedList<SearchSuggestion>(20)
            }
            val initialState = SearchState()
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                getSearchSuggestions = getSearchSuggestions,
                initialState = initialState
            )
            val searchText = "test"

            viewModel.send(NewSearch(searchText, true))
            viewModel.send(NewSearch(searchText, true))

            coVerify(exactly = 1) { searchEvents(searchText) }
            coVerify(exactly = 1) { getSearchSuggestions(searchText) }
        }
    }
}
