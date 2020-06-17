package com.example.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.model.search.SearchSuggestion
import com.example.core.usecase.*
import com.example.core.util.PagedDataList
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.util.addedToFavouritesMessage
import com.example.test.rule.event
import com.example.test.rule.mockLog
import com.example.test.rule.mockedList
import com.example.test.rule.relaxedMockedList
import io.mockk.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
class SearchFlowProcessorTests {
    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockLog()
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
    }

    private fun flowProcessor(
        searchEvents: SearchEvents = mockk(relaxed = true),
        getPagedEventsFlow: GetPagedEventsFlow = GetPagedEventsFlow(testDispatcher),
        saveEvents: SaveEvents = mockk(relaxed = true),
        getSearchSuggestions: GetSearchSuggestions = mockk(relaxed = true),
        saveSearchSuggestion: SaveSearchSuggestion = mockk(relaxed = true),
        connectedStateProvider: ConnectedStateProvider = mockk(relaxed = true),
        ioDispatcher: CoroutineDispatcher = testDispatcher
    ): SearchFlowProcessor = SearchFlowProcessor(
        searchEvents,
        getPagedEventsFlow,
        saveEvents,
        getSearchSuggestions,
        saveSearchSuggestion,
        connectedStateProvider,
        ioDispatcher
    )

    private fun SearchFlowProcessor.updates(
        intents: Flow<SearchIntent> = mockk(relaxed = true),
        currentState: () -> SearchState = mockk { every { this@mockk() } returns SearchState() },
        states: StateFlow<SearchState> = mockk(relaxed = true),
        intent: suspend (SearchIntent) -> Unit = mockk(relaxed = true),
        signal: suspend (SearchSignal) -> Unit = mockk(relaxed = true)
    ): Flow<SearchStateUpdate> = updates(testScope, intents, currentState, states, intent, signal)

    @Test
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

    //TODO: newSearch tests: searchEventsUpdates
    //loadMoreResultsUpdates tests: filter, searchEventsUpdates
    //connectedStateProvider test

    @Test
    fun initialSearchTextTest() = testScope.runBlockingTest {
        val searchText = "test"
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState(searchText = searchText)
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
        coVerify(exactly = 1) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
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

    @Test
    fun addToFavouritesTest() = testScope.runBlockingTest {
        val saveEvents = mockk<SaveEvents>(relaxed = true)
        val selectableEvents = mockedList(20) { event(it) }
            .mapIndexed { index, event -> Selectable(event, index % 2 == 0) }
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState(
                events = PagedDataList(selectableEvents)
            )
        }

        abstract class Signal {
            abstract suspend operator fun invoke(signal: SearchSignal)
        }

        val signal = mockk<Signal>(relaxed = true)

        val updates = flowProcessor(saveEvents = saveEvents)
            .updates(
                intents = flowOf(SearchIntent.AddToFavouritesClicked),
                currentState = currentState,
                signal = signal::invoke
            )
            .toList()

        val selectedEvents = selectableEvents.filter { it.selected }.map { it.item }
        verify(exactly = 2) { currentState() }
        coVerify(exactly = 1) { saveEvents(selectedEvents) }
        coVerify(exactly = 1) { signal(SearchSignal.FavouritesSaved) }
        assert(updates.size == 1)
        val update = updates.first()
        assert(
            update is SearchStateUpdate.Events.AddedToFavourites
                    && update.snackbarText == addedToFavouritesMessage(eventsCount = selectedEvents.size)
        )
    }

    @Test
    fun eventLongClickedTest() = testScope.runBlockingTest {
        val event = event()
        val updates = flowProcessor()
            .updates(intents = flowOf(SearchIntent.EventLongClicked(event)))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == SearchStateUpdate.ToggleEventSelection(event))
    }

    @Test
    fun clearSelectionTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(SearchIntent.ClearSelectionClicked))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == SearchStateUpdate.ClearSelection)
    }

    @Test
    fun hideSnackbarTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(SearchIntent.HideSnackbar))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == SearchStateUpdate.HideSnackbar)
    }
}