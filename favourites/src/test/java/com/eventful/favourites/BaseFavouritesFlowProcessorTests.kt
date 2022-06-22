package com.eventful.favourites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.eventful.core.usecase.event.DeleteEvents
import com.eventful.core.usecase.event.GetSavedEventsFlow
import com.eventful.test.mockLog
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule

@ExperimentalCoroutinesApi
@FlowPreview
internal abstract class BaseFavouritesFlowProcessorTests {
    private val testDispatcher = TestCoroutineDispatcher()
    protected val testScope = TestCoroutineScope(testDispatcher)

    @get:Rule val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockLog()
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
    }

    protected fun flowProcessor(
        getSavedEventsFlow: GetSavedEventsFlow = mockk(relaxed = true),
        deleteEvents: DeleteEvents = mockk(relaxed = true),
        ioDispatcher: CoroutineDispatcher = testDispatcher,
        loadFavouritesOnStart: Boolean = false
    ): FavouritesFlowProcessor =
        FavouritesFlowProcessor(
            getSavedEventsFlow, deleteEvents, ioDispatcher, loadFavouritesOnStart)

    protected fun FavouritesFlowProcessor.updates(
        intents: Flow<FavouritesIntent> = mockk(relaxed = true),
        currentState: () -> FavouritesState = mockk(relaxed = true),
        states: Flow<FavouritesState> = mockk(relaxed = true),
        intent: suspend (FavouritesIntent) -> Unit = mockk(relaxed = true),
        signal: suspend (FavouritesSignal) -> Unit = mockk(relaxed = true)
    ): Flow<FavouritesStateUpdate> {
        return updates(testScope, intents, currentState, states, intent, signal)
    }
}
