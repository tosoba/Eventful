package com.example.favourites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEventsFlow
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.Event
import com.example.core.util.Initial
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.ext.takeWhileInclusive
import com.example.test.rule.event
import com.example.test.rule.mockedList
import com.example.test.rule.relaxedMockedList
import com.google.android.material.snackbar.Snackbar
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
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
internal class FavouritesViewModelTest {

    companion object {
        private const val initialEventsSize = 20
        private const val afterLoadMoreSize = 30
    }

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

    @Test
    fun `GivenFavouritesVM WhenInitializedAndGetSavedEventsReturnsSuccessfully ThenEventsAreStored`() {
        testScope.runBlockingTest {
            val getSavedEventsFlow: GetSavedEventsFlow = mockk {
                coEvery { this@mockk(any()) } returns flowOf(
                    relaxedMockedList<Event>(initialEventsSize)
                )
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)

            pauseDispatcher()
            val viewModel = FavouritesViewModel(getSavedEventsFlow, deleteEvents, testDispatcher)
            val states = viewModel.states
                .onEach { if (it.events.status is Initial) resumeDispatcher() }
                .takeWhileInclusive { it.events.status !is LoadedSuccessfully }
                .toList()

            coVerify(exactly = 1) { getSavedEventsFlow(FavouritesViewModel.limitIncrement) }
            assert(states.size == 2)
            val initialState = states.first()
            assert(
                initialState.events.data.isEmpty()
                        && initialState.events.status is Initial
                        && initialState.limit == 0
                        && !initialState.events.limitHit
            )
            val loadedState = states.last()
            assert(
                loadedState.events.data.size == initialEventsSize
                        && loadedState.events.status is LoadedSuccessfully
                        && loadedState.limit == initialEventsSize
                        && !loadedState.events.limitHit
            )
        }
    }

    @Test
    fun `GivenFavouritesVM WhenThereAreNoMoreEventsToLoad SameEventsAreReturned`() {
        testScope.runBlockingTest {
            val getSavedEventsFlow: GetSavedEventsFlow = mockk {
                coEvery { this@mockk(any()) } returns flowOf(
                    relaxedMockedList<Event>(
                        initialEventsSize
                    )
                )
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEventsFlow, deleteEvents, testDispatcher)

            viewModel.intent(LoadFavourites)

            // try load more after limit was hit
            val states = mutableListOf<FavouritesState>()
            launch {
                viewModel.states.takeWhileInclusive { !it.events.limitHit }.toList(states)
            }
            viewModel.intent(LoadFavourites)

            coVerify(exactly = 1) {
                getSavedEventsFlow(initialEventsSize + FavouritesViewModel.limitIncrement)
            }
            coVerify {
                getSavedEventsFlow(initialEventsSize + 2 * FavouritesViewModel.limitIncrement) wasNot called
            }
            val finalState = states.last()
            assert(
                finalState.events.status is LoadedSuccessfully
                        && finalState.events.data.size == initialEventsSize
                        && finalState.limit == initialEventsSize
                        && finalState.events.limitHit
            )
        }
    }

    @Test
    fun `GivenFavouritesVM WhenThereAreMoreEventsToLoad MoreEventsAreReturned`() {
        testScope.runBlockingTest {
            val getSavedEventsFlow: GetSavedEventsFlow = mockk {
                coEvery { this@mockk(FavouritesViewModel.limitIncrement) } returns flowOf(
                    relaxedMockedList<Event>(initialEventsSize)
                )
                coEvery { this@mockk(FavouritesViewModel.limitIncrement + initialEventsSize) } returns flowOf(
                    relaxedMockedList<Event>(afterLoadMoreSize)
                )
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)

            val viewModel = FavouritesViewModel(getSavedEventsFlow, deleteEvents, testDispatcher)

            val states = mutableListOf<FavouritesState>()
            launch {
                viewModel.states.takeWhileInclusive { it.limit != afterLoadMoreSize }.toList(states)
            }
            viewModel.intent(LoadFavourites)

            coVerify(exactly = 1) {
                getSavedEventsFlow(initialEventsSize + FavouritesViewModel.limitIncrement)
            }
            val finalState = states.last()
            assert(
                finalState.events.status is LoadedSuccessfully
                        && finalState.events.data.size == afterLoadMoreSize
                        && finalState.limit == afterLoadMoreSize
                        && !finalState.events.limitHit
            )
        }
    }

    @Test
    fun `GivenFavouritesVMWithInitialEvents WhenEventIsLongClicked EventSelectionChanges`() {
        testScope.runBlockingTest {
            val eventsList = relaxedMockedList<Event>(initialEventsSize)
            val getSavedEventsFlow: GetSavedEventsFlow = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList)
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEventsFlow, deleteEvents, testDispatcher)

            viewModel.intent(EventLongClicked(eventsList.first()))
            assert(viewModel.state.events.data.first().selected)

            viewModel.intent(EventLongClicked(eventsList.first()))
            assert(!viewModel.state.events.data.first().selected)
        }
    }

    @Test
    fun `GivenFavouritesVMWithInitialEvents WhenClearSelectionClicked NoEventsAreSelected`() {
        testScope.runBlockingTest {
            val eventsList = mockedList(initialEventsSize) { event(it) }
            val getSavedEventsFlow: GetSavedEventsFlow = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList)
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEventsFlow, deleteEvents, testDispatcher)

            viewModel.intent(EventLongClicked(eventsList.first()))
            viewModel.intent(EventLongClicked(eventsList.last()))
            viewModel.intent(ClearSelectionClicked)

            assert(!viewModel.state.events.data.any { it.selected })
        }
    }

    @Test
    fun `GivenFavouritesVMWithInitialEvents WhenRemoveFromFavouritesClicked DeleteEventsIsCalled`() {
        testScope.runBlockingTest {
            val eventsList = mockedList(initialEventsSize) { event(it) }
            val getSavedEventsFlow: GetSavedEventsFlow = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList)
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEventsFlow, deleteEvents, testDispatcher)

            val signals = mutableListOf<FavouritesSignal>()
            launch {
                viewModel.signals.take(1).toList(signals)
            }

            viewModel.intent(EventLongClicked(eventsList.first()))
            viewModel.intent(EventLongClicked(eventsList.last()))
            viewModel.intent(RemoveFromFavouritesClicked)

            coVerify(exactly = 1) { deleteEvents(listOf(eventsList.first(), eventsList.last())) }
            assert(signals.size == 1 && signals.first() == FavouritesSignal.FavouritesRemoved)
            val finalSnackbarState = viewModel.state.snackbarState
            assert(
                finalSnackbarState is SnackbarState.Shown
                        && finalSnackbarState.text == "2 events were removed from favourites"
                        && finalSnackbarState.length == Snackbar.LENGTH_SHORT
            )
        }
    }
}
