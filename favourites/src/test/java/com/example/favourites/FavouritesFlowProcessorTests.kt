package com.example.favourites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.core.util.DataList
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.base.removedFromFavouritesMessage
import com.example.test.rule.event
import com.example.test.rule.mockLog
import com.example.test.rule.mockedList
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
        ioDispatcher: CoroutineDispatcher = testDispatcher,
        loadFavouritesOnStart: Boolean = false
    ): FavouritesFlowProcessor = FavouritesFlowProcessor(
        getSavedEventsFlow,
        deleteEvents,
        ioDispatcher,
        loadFavouritesOnStart
    )

    private fun FavouritesFlowProcessor.updates(
        intents: Flow<FavouritesIntent> = mockk(relaxed = true),
        currentState: () -> FavouritesState = mockk(relaxed = true),
        states: StateFlow<FavouritesState> = mockk(relaxed = true),
        intent: suspend (FavouritesIntent) -> Unit = mockk(relaxed = true),
        signal: suspend (FavouritesSignal) -> Unit = mockk(relaxed = true)
    ): Flow<FavouritesStateUpdate> {
        return updates(testScope, intents, currentState, states, intent, signal)
    }

    @Test
    fun loadFavouritesTest() = testScope.runBlockingTest {
        val events = mockedList(20) { event(it) }
        val getSavedEventsFlow = mockk<GetSavedEventsFlow> {
            every { this@mockk(any()) } returns flowOf(events)
        }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        val updates = flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
            .updates(
                intents = flowOf(FavouritesIntent.LoadFavourites),
                currentState = currentState
            )
            .toList()

        verify(exactly = 1) { getSavedEventsFlow(limit) }
        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.Events(events))
    }

    @Test
    fun loadFavouritesMultipleEmissionsTest() = testScope.runBlockingTest {
        val events1stEmission = mockedList(20) { event(it) }
        val events2ndEmission = mockedList(25) { event(it) }
        val getSavedEventsFlow = mockk<GetSavedEventsFlow> {
            every { this@mockk(any()) } returns flowOf(events1stEmission, events2ndEmission)
        }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        val updates = flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
            .updates(
                intents = flowOf(FavouritesIntent.LoadFavourites),
                currentState = currentState
            )
            .toList()

        verify(exactly = 1) { getSavedEventsFlow(limit) }
        assert(updates.size == 2)
        assert(updates.first() == FavouritesStateUpdate.Events(events1stEmission))
        assert(updates.last() == FavouritesStateUpdate.Events(events2ndEmission))
    }

    @Test
    fun loadingFavouritesOnStartTest() = testScope.runBlockingTest {
        val getSavedEventsFlow = mockk<GetSavedEventsFlow> {
            every { this@mockk(any()) } returns flowOf(mockedList(20) { event(it) })
        }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        flowProcessor(
            getSavedEventsFlow = getSavedEventsFlow,
            loadFavouritesOnStart = true
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).toList()

        verify(exactly = 1) { getSavedEventsFlow(limit) }
    }

    @Test
    fun noLoadingFavouritesOnStartTest() = testScope.runBlockingTest {
        val getSavedEventsFlow = mockk<GetSavedEventsFlow>(relaxed = true)
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val limit = currentState().limit + FavouritesFlowProcessor.limitIncrement

        flowProcessor(
            getSavedEventsFlow = getSavedEventsFlow,
            loadFavouritesOnStart = false
        ).updates(
            intents = emptyFlow(),
            currentState = currentState
        ).toList()

        verify(exactly = 0) { getSavedEventsFlow(limit) }
    }

    @Test
    fun newSearchTest() = testScope.runBlockingTest {
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState()
        }
        val searchText = "test"

        val updates = flowProcessor()
            .updates(
                intents = flowOf(FavouritesIntent.NewSearch(searchText)),
                currentState = currentState
            )
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.SearchTextUpdate(searchText))
    }

    @Test
    fun removeFromFavouritesTest() = testScope.runBlockingTest {
        val deleteEvents = mockk<DeleteEvents>(relaxed = true)
        val selectableEvents = mockedList(20) { event(it) }
            .mapIndexed { index, event -> Selectable(event, index % 2 == 0) }
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState(
                events = DataList(selectableEvents)
            )
        }

        abstract class Signal {
            abstract suspend operator fun invoke(signal: FavouritesSignal)
        }

        val signal = mockk<Signal>(relaxed = true)

        val updates = flowProcessor(deleteEvents = deleteEvents)
            .updates(
                intents = flowOf(FavouritesIntent.RemoveFromFavouritesClicked),
                currentState = currentState,
                signal = signal::invoke
            )
            .toList()

        val selectedEvents = selectableEvents.filter { it.selected }.map { it.item }
        verify(exactly = 1) { currentState() }
        coVerify(exactly = 1) { deleteEvents(selectedEvents) }
        coVerify(exactly = 1) { signal(FavouritesSignal.FavouritesRemoved) }
        assert(updates.size == 1)
        val update = updates.first()
        assert(
            update is FavouritesStateUpdate.RemovedFromFavourites
                    && update.snackbarText == removedFromFavouritesMessage(
                eventsCount = selectedEvents.size
            )
        )
    }

    @Test
    fun loadFavouritesLimitHitTest() = testScope.runBlockingTest {
        val getSavedEventsFlow = mockk<GetSavedEventsFlow>(relaxed = true)
        val currentState = mockk<() -> FavouritesState> {
            every { this@mockk() } returns FavouritesState(events = DataList(limitHit = true))
        }

        flowProcessor(getSavedEventsFlow = getSavedEventsFlow)
            .updates(
                intents = flowOf(FavouritesIntent.LoadFavourites),
                currentState = currentState
            )
            .launchIn(testScope)

        verify(exactly = 1) { currentState() }
        verify(exactly = 0) { getSavedEventsFlow(any()) }
    }

    @Test
    fun eventLongClickedTest() = testScope.runBlockingTest {
        val event = event()

        val updates = flowProcessor()
            .updates(intents = flowOf(FavouritesIntent.EventLongClicked(event)))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.ToggleEventSelection(event))
    }

    @Test
    fun clearSelectionTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(FavouritesIntent.ClearSelectionClicked))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.ClearSelection)
    }

    @Test
    fun hideSnackbarTest() = testScope.runBlockingTest {
        val updates = flowProcessor()
            .updates(intents = flowOf(FavouritesIntent.HideSnackbar))
            .toList()

        assert(updates.size == 1)
        assert(updates.first() == FavouritesStateUpdate.HideSnackbar)
    }
}
