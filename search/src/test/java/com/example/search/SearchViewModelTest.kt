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
import com.example.test.rule.relaxedMockedList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
}
