package com.example.search

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.coreandroid.util.Initial
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.LoadingFailed
import com.example.coreandroid.util.takeWhileInclusive
import com.example.test.rule.MainDispatcherRule
import com.example.test.rule.getEvents
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private fun searchViewModelWith(
        searchEvents: SearchEvents? = null,
        getSearchSuggestions: GetSeachSuggestions? = null,
        saveSuggestion: SaveSuggestion? = null,
        initialState: SearchState = SearchState.INITIAL
    ): SearchViewModel = SearchViewModel(
        searchEvents ?: mockk(relaxed = true),
        getSearchSuggestions ?: mockk(relaxed = true),
        saveSuggestion ?: mockk(relaxed = true),
        testDispatcher, initialState
    )

    @Test
    fun `GivenSearchVM WhenInitialized StateIsInitial`() = runBlocking {
        val viewModel = searchViewModelWith()

        val (_, _, events) = viewModel.state.first()
        val (value, status, _, _) = events
        assert(value.isEmpty() && status is Initial)
    }

    @Test
    fun `GivenSearchVMAndNoEventsLoaded WhenOnNotConnected StateStatusIsLoadingError`() =
        runBlocking {
            val viewModel = searchViewModelWith()

            viewModel.onNotConnected()

            val (_, _, events) = viewModel.state.first { it.events.status !is Initial }
            val (value, status, _, _) = events
            assert(value.isEmpty() && status is LoadingFailed<*> && status.error == SearchError.NotConnected)
        }

    @Test
    fun `GivenSearchVMAndValidSearchText WhenSaveSuggestion SuggestionIsSaved`() =
        runBlocking {
            val saveSuggestion = mockk<SaveSuggestion>(relaxed = true)
            val viewModel = searchViewModelWith(saveSuggestion = saveSuggestion)
            val validSearchText = "suggestion"

            viewModel.insertNewSuggestion(validSearchText)

            coVerify { saveSuggestion(validSearchText) }
        }

    @Test
    fun `GivenSearchVMAndInvalidSearchText WhenSaveSuggestion SuggestionIsNotSaved`() =
        runBlocking {
            val saveSuggestion = mockk<SaveSuggestion>(relaxed = true)
            val viewModel = searchViewModelWith(saveSuggestion = saveSuggestion)
            val blankSearchText = ""
            val invalidSearchText = "inv"

            viewModel.insertNewSuggestion(blankSearchText)
            viewModel.insertNewSuggestion(invalidSearchText)

            coVerify {
                listOf(
                    saveSuggestion(blankSearchText),
                    saveSuggestion(invalidSearchText)
                ) wasNot called
            }
        }

    @Test
    fun `GivenSearchVMAndSearchText WhenGetSuggestions SuggestionsAreLoaded`() =
        runBlocking {
            val searchText = "search"
            val numberOfReturnedSuggestions = 10
            val getSearchSuggestions = mockk<GetSeachSuggestions> {
                coEvery { this@mockk(searchText) } returns (1..numberOfReturnedSuggestions).map { mockk<SearchSuggestion>() }
            }
            val viewModel = searchViewModelWith(getSearchSuggestions = getSearchSuggestions)

            viewModel.loadSearchSuggestions(searchText)

            coVerify { getSearchSuggestions(searchText) }
            val suggestions = viewModel.state
                .first { it.searchSuggestions.isNotEmpty() }
                .searchSuggestions
            assert(suggestions.size == numberOfReturnedSuggestions)
        }

    @Test
    fun `GivenSearchVMAndRepeatedSearchText WhenSearch SearchEventsIsNotCalled`() = runBlocking {
        val searchText = "search"
        val searchEvents = mockk<SearchEvents>()
        val viewModel = searchViewModelWith(
            searchEvents = searchEvents,
            initialState = SearchState(searchText, mockk(), mockk())
        )
        viewModel.search(searchText, retry = false)
        coVerify { searchEvents(searchText) wasNot called }
    }

    @Test
    fun `GivenSearchVMAndSearchText WhenSearchReturnsSuccessfully ResultsAreStoredInState`() =
        runBlocking {
            val searchText = "search"
            val expectedEventsSize = 20
            val expectedTotalPages = 1
            val expectedCurrentPage = 1
            val searchEvents = mockk<SearchEvents> {
                coEvery { this@mockk(searchText) } returns Resource.Success(
                    PagedResult<IEvent>(
                        getEvents(expectedEventsSize),
                        expectedCurrentPage,
                        expectedTotalPages
                    )
                )
            }
            val viewModel = searchViewModelWith(searchEvents = searchEvents)

            val states = mutableListOf<SearchState>()
            val job = launch {
                viewModel.state
                    .takeWhileInclusive { (_, _, events) -> events.status !is LoadedSuccessfully }
                    .collect { states.add(it) }
            }
            viewModel.search(searchText)
            job.join()

            coVerify { searchEvents(searchText) }
            val (_, _, eventsDataList) = states.last()
            val (events, status, offset, totalPages) = eventsDataList
            assert(
                events.size == 20
                        && status is LoadedSuccessfully
                        && offset == expectedCurrentPage + 1
                        && totalPages == expectedTotalPages
            )
        }
}