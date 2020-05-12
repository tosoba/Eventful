package com.example.favourites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.Initial
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.takeWhileInclusive
import com.example.test.rule.event
import com.example.test.rule.mockedList
import com.example.test.rule.relaxedMockedList
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
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
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(any()) } returns flowOf(
                    relaxedMockedList<Event>(initialEventsSize)
                )
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)

            pauseDispatcher()
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)
            val states = viewModel.states
                .onEach { if (it.events.status is Initial) resumeDispatcher() }
                .takeWhileInclusive { it.events.status !is LoadedSuccessfully }
                .toList()

            coVerify(exactly = 1) { getSavedEvents(FavouritesViewModel.limitIncrement) }
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
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(any()) } returns flowOf(
                    relaxedMockedList<Event>(
                        initialEventsSize
                    )
                )
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

            viewModel.send(LoadFavourites)

            // try load more after limit was hit
            val states = mutableListOf<FavouritesState>()
            val job = launch {
                viewModel.states.takeWhileInclusive { !it.events.limitHit }.toList(states)
            }
            viewModel.send(LoadFavourites)
            job.join()

            coVerify(exactly = 1) {
                getSavedEvents(initialEventsSize + FavouritesViewModel.limitIncrement)
            }
            coVerify {
                getSavedEvents(initialEventsSize + 2 * FavouritesViewModel.limitIncrement) wasNot called
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
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(FavouritesViewModel.limitIncrement) } returns flowOf(
                    relaxedMockedList<Event>(initialEventsSize)
                )
                coEvery { this@mockk(FavouritesViewModel.limitIncrement + initialEventsSize) } returns flowOf(
                    relaxedMockedList<Event>(afterLoadMoreSize)
                )
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)

            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

            val states = mutableListOf<FavouritesState>()
            val job = launch {
                viewModel.states.takeWhileInclusive { it.limit != afterLoadMoreSize }.toList(states)
            }
            viewModel.send(LoadFavourites)
            job.join()

            coVerify(exactly = 1) {
                getSavedEvents(initialEventsSize + FavouritesViewModel.limitIncrement)
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
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList)
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

            viewModel.send(EventLongClicked(eventsList.first()))
            assert(viewModel.state.events.data.first().selected)

            viewModel.send(EventLongClicked(eventsList.first()))
            assert(!viewModel.state.events.data.first().selected)
        }
    }

    @Test
    fun `GivenFavouritesVMWithInitialEvents WhenClearSelectionClicked NoEventsAreSelected`() {
        testScope.runBlockingTest {
            val eventsList = mockedList(initialEventsSize) { event(it) }
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList)
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

            viewModel.send(EventLongClicked(eventsList.first()))
            viewModel.send(EventLongClicked(eventsList.last()))
            viewModel.send(ClearSelectionClicked)

            assert(!viewModel.state.events.data.any { it.selected })
        }
    }

    @Test
    fun `GivenFavouritesVMWithInitialEvents WhenRemoveFromFavouritesClicked DeleteEventsIsCalled`() {
        testScope.runBlockingTest {
            val eventsList = mockedList(initialEventsSize) { event(it) }
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList)
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

            viewModel.send(EventLongClicked(eventsList.first()))
            viewModel.send(EventLongClicked(eventsList.last()))
            viewModel.send(RemoveFromFavouritesClicked)

            coVerify(exactly = 1) { deleteEvents(listOf(eventsList.first(), eventsList.last())) }
            assert(viewModel.signals.value == FavouritesSignal.FavouritesRemoved)
        }
    }
}
