package com.example.nearby

import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.usecase.SaveEvents
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
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
internal abstract class BaseNearbyFlowProcessorTests {
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
        searchEvents: GetNearbyEvents = mockk(relaxed = true),
        saveEvents: SaveEvents = mockk(relaxed = true),
        getPagedEventsFlow: GetPagedEventsFlow = GetPagedEventsFlow(testDispatcher),
        connectedStateProvider: ConnectedStateProvider = mockk(relaxed = true),
        locationStateProvider: LocationStateProvider = mockk(relaxed = true),
        ioDispatcher: CoroutineDispatcher = testDispatcher
    ): NearbyFlowProcessor = NearbyFlowProcessor(
        searchEvents,
        saveEvents,
        getPagedEventsFlow,
        connectedStateProvider,
        locationStateProvider,
        ioDispatcher
    )

    protected fun NearbyFlowProcessor.updates(
        intents: Flow<NearbyIntent> = mockk(relaxed = true),
        currentState: () -> NearbyState = mockk { every { this@mockk() } returns NearbyState() },
        states: StateFlow<NearbyState> = mockk(relaxed = true),
        intent: suspend (NearbyIntent) -> Unit = mockk(relaxed = true),
        signal: suspend (NearbySignal) -> Unit = mockk(relaxed = true)
    ): Flow<NearbyStateUpdate> = updates(testScope, intents, currentState, states, intent, signal)

    protected abstract class Signal {
        abstract suspend operator fun invoke(signal: NearbySignal)
    }
}