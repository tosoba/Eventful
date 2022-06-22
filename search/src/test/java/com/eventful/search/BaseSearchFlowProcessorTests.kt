package com.eventful.search

import com.eventful.core.android.provider.ConnectedStateProvider
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.usecase.event.SaveEvents
import com.eventful.core.usecase.event.SearchEvents
import com.eventful.core.usecase.search.GetSearchSuggestions
import com.eventful.core.usecase.search.SaveSearchSuggestion
import com.eventful.test.mockLog
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
        getPagedEvents: GetPagedEvents = GetPagedEvents(testDispatcher),
        saveEvents: SaveEvents = mockk(relaxed = true),
        getSearchSuggestions: GetSearchSuggestions = mockk(relaxed = true),
        saveSearchSuggestion: SaveSearchSuggestion = mockk(relaxed = true),
        connectedStateProvider: ConnectedStateProvider = mockk(relaxed = true),
        ioDispatcher: CoroutineDispatcher = testDispatcher
    ): SearchFlowProcessor =
        SearchFlowProcessor(
            searchEvents,
            getPagedEvents,
            saveEvents,
            getSearchSuggestions,
            saveSearchSuggestion,
            connectedStateProvider,
            ioDispatcher)

    protected fun SearchFlowProcessor.updates(
        intents: Flow<SearchIntent> = mockk(relaxed = true),
        currentState: () -> SearchState = mockk { every { this@mockk() } returns SearchState() },
        states: StateFlow<SearchState> = mockk(relaxed = true),
        intent: suspend (SearchIntent) -> Unit = mockk(relaxed = true),
        signal: suspend (SearchSignal) -> Unit = mockk(relaxed = true)
    ): Flow<SearchStateUpdate> = updates(testScope, intents, currentState, states, intent, signal)
}
