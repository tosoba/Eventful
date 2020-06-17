package com.example.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.usecase.*
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.test.rule.event
import com.example.test.rule.mockLog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
    ): Flow<SearchStateUpdate> {
        return updates(testScope, intents, currentState, states, intent, signal)
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