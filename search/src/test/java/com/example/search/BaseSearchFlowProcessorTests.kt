package com.example.search

import com.example.core.usecase.*
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.test.rule.mockLog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
@FlowPreview
internal abstract class BaseSearchFlowProcessorTests {
    private val testDispatcher = TestCoroutineDispatcher()
    protected val testScope = TestCoroutineScope(testDispatcher)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockLog()
    }

    @AfterEach
    fun cleanUp() {
        Dispatchers.resetMain()
    }

    protected fun flowProcessor(
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

    protected fun SearchFlowProcessor.updates(
        intents: Flow<SearchIntent> = mockk(relaxed = true),
        currentState: () -> SearchState = mockk { every { this@mockk() } returns SearchState() },
        states: StateFlow<SearchState> = mockk(relaxed = true),
        intent: suspend (SearchIntent) -> Unit = mockk(relaxed = true),
        signal: suspend (SearchSignal) -> Unit = mockk(relaxed = true)
    ): Flow<SearchStateUpdate> = updates(testScope, intents, currentState, states, intent, signal)
}