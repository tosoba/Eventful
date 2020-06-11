package com.example.favourites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.core.util.DataList
import com.example.test.rule.mockLog
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
class FavouritesFlowProcessorTests {
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
        getSavedEventsFlow: GetSavedEventsFlow = mockk(relaxed = true),
        deleteEvents: DeleteEvents = mockk(relaxed = true),
        ioDispatcher: CoroutineDispatcher = testDispatcher
    ): FavouritesFlowProcessor = FavouritesFlowProcessor(
        getSavedEventsFlow,
        deleteEvents,
        ioDispatcher
    )

    private fun FavouritesFlowProcessor.updates(
        intents: Flow<FavouritesIntent> = mockk(relaxed = true),
        currentState: () -> FavouritesState = mockk(relaxed = true),
        intent: suspend (FavouritesIntent) -> Unit = mockk(relaxed = true),
        signal: suspend (FavouritesSignal) -> Unit = mockk(relaxed = true),
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): Flow<FavouritesStateUpdate> {
        return updates(testScope, intents, currentState, intent, signal, savedStateHandle)
    }

    @Test
    fun loadFavouritesLimitHitTest() = testScope.runBlockingTest {
        val getSavedEventsFlow = mockk<GetSavedEventsFlow>(relaxed = true)
        val processor = flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState(events = DataList(limitHit = true))
        }

        processor
            .updates(
                intents = flowOf(),
                currentState = currentState
            )
            .launchIn(testScope)

        verify(exactly = 1) { currentState() }
        verify(exactly = 0) { getSavedEventsFlow(any()) }
    }
}