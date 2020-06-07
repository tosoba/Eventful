package com.example.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.model.Resource
import com.example.core.model.PagedResult
import com.example.core.model.search.SearchSuggestion
import com.example.core.model.event.IEvent
import com.example.core.usecase.GetSearchSuggestions
import com.example.core.usecase.SaveEvents
import com.example.core.usecase.SaveSuggestion
import com.example.core.usecase.SearchEvents
import com.example.core.util.*
import com.example.core.util.ext.takeWhileInclusive
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.model.Selectable
import com.example.test.rule.event
import com.example.test.rule.mockedList
import com.example.test.rule.onPausedDispatcher
import com.example.test.rule.relaxedMockedList
import com.google.android.material.snackbar.Snackbar
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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
        getSearchSuggestions: GetSearchSuggestions = mockk(relaxed = true),
        saveSuggestion: SaveSuggestion = mockk(relaxed = true),
        connectedStateProvider: ConnectedStateProvider = mockk(relaxed = true),
        initialState: SearchState = SearchState()
    ): SearchViewModel = SearchViewModel(
        searchEvents = searchEvents,
        saveEvents = saveEvents,
        getSearchSuggestions = getSearchSuggestions,
        saveSuggestion = saveSuggestion,
        connectedStateProvider = connectedStateProvider,
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
            val getSearchSuggestions = mockk<GetSearchSuggestions> {
                coEvery { this@mockk(any()) } returns emptyList<SearchSuggestion>()
            }
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                saveSuggestion = saveSuggestion,
                getSearchSuggestions = getSearchSuggestions
            )
            val searchText = "test"

            viewModel.intent(NewSearch(searchText, true))

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
            val getSearchSuggestions = mockk<GetSearchSuggestions> {
                coEvery { this@mockk(any()) } returns emptyList<SearchSuggestion>()
            }
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                saveSuggestion = saveSuggestion,
                getSearchSuggestions = getSearchSuggestions
            )
            val searchText = "test"

            viewModel.intent(NewSearch(searchText, false))

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
                        mockedList<IEvent>(returnedEventsListSize) { event(it) },
                        currentPage,
                        totalPages
                    )
                )
            }
            val returnedSuggestionsListSize = 20
            val getSearchSuggestions = mockk<GetSearchSuggestions> {
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
                viewModel.intent(NewSearch(searchText, false))
                viewModel.states.takeWhileInclusive { it.events.status !is LoadedSuccessfully }
                    .toList()
            }

            assert(states.size == 4)

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
                loadingState.searchText == searchText
                        && loadingState.searchSuggestions == initialState.searchSuggestions
                        && loadingState.snackbarState == initialState.snackbarState
            )
            assert(
                loadingState.events.data.isEmpty()
                        && loadingState.events.status is Loading
                        && loadingState.events.offset == initialState.events.offset
                        && loadingState.events.limit == initialState.events.limit
            )

            val loadedSuggestionsState = states[2]
            assert(
                loadedSuggestionsState.searchText == searchText
                        && loadedSuggestionsState.searchSuggestions.size == returnedSuggestionsListSize
                        && loadedSuggestionsState.snackbarState == initialState.snackbarState
            )
            assert(
                loadedSuggestionsState.events.data.isEmpty()
                        && loadedSuggestionsState.events.status is Loading
                        && loadedSuggestionsState.events.offset == initialState.events.offset
                        && loadedSuggestionsState.events.limit == initialState.events.limit
            )

            val loadedEventsState = states.last()
            assert(
                loadedEventsState.searchText == searchText
                        && loadedEventsState.searchSuggestions.size == returnedSuggestionsListSize
                        && loadedEventsState.snackbarState == initialState.snackbarState
            )
            assert(
                loadedEventsState.events.data.size == returnedEventsListSize
                        && loadedEventsState.events.status is LoadedSuccessfully
                        && loadedEventsState.events.offset == currentPage + 1
                        && loadedEventsState.events.limit == totalPages
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
            val getSearchSuggestions = mockk<GetSearchSuggestions> {
                coEvery { this@mockk(any()) } returns relaxedMockedList<SearchSuggestion>(20)
            }
            val initialState = SearchState()
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                getSearchSuggestions = getSearchSuggestions,
                initialState = initialState
            )
            val searchText = "test"

            viewModel.intent(NewSearch(searchText, true))
            viewModel.intent(NewSearch(searchText, true))

            coVerify(exactly = 1) { searchEvents(searchText) }
            coVerify(exactly = 1) { getSearchSuggestions(searchText) }
        }
    }

    @Test
    fun `GivenSearchVM WhenLoadMoreResultsIsCalled ResultsAreLoadedUntilLimitIsHit`() {
        testScope.runBlockingTest {
            val returnedEventsListSize = 20
            val totalPages = 2
            val searchEvents = mockk<SearchEvents> {
                coEvery { this@mockk(any()) } returns Resource.successWith(
                    PagedResult(
                        mockedList<IEvent>(returnedEventsListSize) { event(it) },
                        0,
                        totalPages
                    )
                )

                coEvery { this@mockk(any(), 1) } returns Resource.successWith(
                    PagedResult(
                        mockedList<IEvent>(returnedEventsListSize) { event(it + 20) },
                        1,
                        totalPages
                    )
                )
            }
            val getSearchSuggestions = mockk<GetSearchSuggestions> {
                coEvery { this@mockk(any()) } returns relaxedMockedList<SearchSuggestion>(1)
            }
            val initialState = SearchState()
            val viewModel = searchViewModel(
                searchEvents = searchEvents,
                getSearchSuggestions = getSearchSuggestions,
                initialState = initialState
            )
            val searchText = "test"

            viewModel.intent(NewSearch(searchText, false))
            viewModel.intent(LoadMoreResults)
            viewModel.intent(LoadMoreResults)

            coVerify(exactly = 1) { searchEvents(searchText, 1) }
            val (finalSearchText, _, finalEvents, _) = viewModel.state
            assert(
                finalSearchText == searchText
                        && !finalEvents.canLoadMore
                        && finalEvents.offset == totalPages
                        && finalEvents.status is LoadedSuccessfully
                        && finalEvents.data.size == returnedEventsListSize * 2
            )
        }
    }

    @Test
    fun `GivenSearchVMWithInitialEvents WhenEventIsLongClicked EventSelectionChanges`() {
        testScope.runBlockingTest {
            val eventsList = mockedList(20) { event(it) }
            val viewModel = searchViewModel(
                initialState = SearchState(
                    events = PagedDataList(eventsList.map {
                        Selectable(
                            it
                        )
                    })
                )
            )

            viewModel.intent(EventLongClicked(eventsList.first()))
            assert(viewModel.state.events.data.first().selected)

            viewModel.intent(EventLongClicked(eventsList.first()))
            assert(!viewModel.state.events.data.first().selected)
        }
    }

    @Test
    fun `GivenSearchVMWithInitialEvents WhenClearSelectionClicked NoEventsAreSelected`() {
        testScope.runBlockingTest {
            val eventsList = mockedList(20) { event(it) }
            val viewModel = searchViewModel(
                initialState = SearchState(
                    events = PagedDataList(eventsList.map {
                        Selectable(
                            it
                        )
                    })
                )
            )

            viewModel.intent(EventLongClicked(eventsList.first()))
            viewModel.intent(EventLongClicked(eventsList.last()))
            viewModel.intent(ClearSelectionClicked)

            assert(!viewModel.state.events.data.any { it.selected })
        }
    }

    @Test
    fun `GivenSearchVMWithInitialEvents WhenAddToFavourites SelectedEventsAreAddedToFavourites`() {
        testScope.runBlockingTest {
            val eventsList = mockedList(20) { event(it) }
            val saveEvents = mockk<SaveEvents>(relaxed = true)
            val viewModel = searchViewModel(
                saveEvents = saveEvents,
                initialState = SearchState(
                    events = PagedDataList(eventsList.map {
                        Selectable(
                            it
                        )
                    })
                )
            )

            val signals = mutableListOf<SearchSignal>()
            launch {
                viewModel.signals.take(1).toList(signals)
            }

            viewModel.intent(EventLongClicked(eventsList.first()))
            viewModel.intent(EventLongClicked(eventsList.last()))
            viewModel.intent(AddToFavouritesClicked)

            coVerify(exactly = 1) { saveEvents(listOf(eventsList.first(), eventsList.last())) }
            val (_, _, finalEvents, finalSnackbarState) = viewModel.state
            assert(!finalEvents.data.any { it.selected })
            assert(
                finalSnackbarState is SnackbarState.Shown
                        && finalSnackbarState.text == "2 events were added to favourites"
                        && finalSnackbarState.length == Snackbar.LENGTH_SHORT
            )
            assert(signals.size == 1 && signals.first() == SearchSignal.FavouritesSaved)
        }
    }

    @Test
    fun `GivenSearchVMWithNoEventsAndLoadingFailed WhenConnected EventsAreLoaded`() {
        testScope.runBlockingTest {
            val searchText = "test"
            val searchEvents = mockk<SearchEvents> {
                coEvery { this@mockk(any()) } returns Resource.successWith(
                    PagedResult(relaxedMockedList<IEvent>(20), 0, 1)
                )
            }
            val connectedStateProvider = mockk<ConnectedStateProvider> {
                every { connectedStates } returns flowOf(true)
            }
            searchViewModel(
                searchEvents = searchEvents,
                connectedStateProvider = connectedStateProvider,
                initialState = SearchState(
                    searchText = searchText,
                    events = PagedDataList(
                        status = Failure(
                            null
                        )
                    )
                )
            )

            coVerify(exactly = 1) { searchEvents(searchText) }
        }
    }
}
