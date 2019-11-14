package com.example.search

import com.example.core.model.search.SearchSuggestion
import com.example.core.usecase.GetSeachSuggestions
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.coreandroid.util.Initial
import com.example.coreandroid.util.LoadingFailed
import com.example.test.rule.MainDispatcherRule
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.first
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
        saveSuggestion: SaveSuggestion? = null
    ): SearchViewModel = SearchViewModel(
        searchEvents ?: mockk(relaxed = true),
        getSearchSuggestions ?: mockk(relaxed = true),
        saveSuggestion ?: mockk(relaxed = true),
        testDispatcher
    )

    @Test
    fun `GivenSearchVM WhenInitialized StateIsInitial`() = runBlocking {
        val viewModel = searchViewModelWith()

        val (_, _, events) = viewModel.state.first()
        val (value, status, _, _) = events
        assert(value.isEmpty() && status is Initial)
    }

    @Test
    fun `GivenSearchVM WhenNoEventsLoadedAndOnNotConnected StateStatusIsLoadingError`() =
        runBlocking {
            val viewModel = searchViewModelWith()

            viewModel.onNotConnected()

            val (_, _, events) = viewModel.state.first { it.events.status !is Initial }
            val (value, status, _, _) = events
            assert(value.isEmpty() && status is LoadingFailed<*> && status.error == SearchError.NotConnected)
        }

    @Test
    fun `GivenSearchVMAndValidSearchText WhenSaveSuggestionIsCalled SuggestionIsSaved`() =
        runBlocking {
            val saveSuggestion = mockk<SaveSuggestion>(relaxed = true)
            val viewModel = searchViewModelWith(saveSuggestion = saveSuggestion)
            val validSearchText = "suggestion"

            viewModel.insertNewSuggestion(validSearchText)

            coVerify { saveSuggestion(validSearchText) }
        }

    @Test
    fun `GivenSearchVMAndInvalidSearchText WhenSaveSuggestionIsCalled SuggestionIsNotSaved`() =
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
    fun `GivenSearchVMAndSearchText WhenGetSuggestionsIsCalled SuggestionsAreLoaded`() =
        runBlocking {
            val searchText = "search"
            val numberOfReturnedSuggestions = 10
            val getSearchSuggestions = mockk<GetSeachSuggestions> {
                coEvery { this@mockk.invoke(searchText) } returns (1..numberOfReturnedSuggestions).map { mockk<SearchSuggestion>() }
            }
            val viewModel = searchViewModelWith(getSearchSuggestions = getSearchSuggestions)

            viewModel.loadSearchSuggestions(searchText)

            coVerify { getSearchSuggestions(searchText) }
            val suggestions = viewModel.state
                .first { it.searchSuggestions.isNotEmpty() }
                .searchSuggestions
            assert(suggestions.size == numberOfReturnedSuggestions)
        }
}