package com.example.favourites

import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.util.Initial
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.takeWhileInclusive
import com.example.test.rule.eventsList
import com.example.test.rule.onPausedDispatcher
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
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
                coEvery { this@mockk(any()) } returns flowOf(eventsList(initialEventsSize))
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)

            val states: List<FavouritesState> = onPausedDispatcher {
                FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)
                    .states
                    .takeWhileInclusive { it.events.status !is LoadedSuccessfully }
                    .toList()
            }

            coVerify { getSavedEvents(FavouritesViewModel.limitIncrement) }
            assert(states.size == 3)
            val initialState = states.first()
            assert(
                initialState.events.data.isEmpty()
                        && initialState.events.status is Initial
                        && initialState.limit == 0
                        && !initialState.limitHit
            )
            val loadingState = states[1]
            assert(
                loadingState.events.data.isEmpty()
                        && loadingState.events.status is Loading
                        && loadingState.limit == 0
                        && !loadingState.limitHit
            )
            val loadedState = states.last()
            assert(
                loadedState.events.data.size == initialEventsSize
                        && loadedState.events.status is LoadedSuccessfully
                        && loadedState.limit == initialEventsSize
                        && !loadedState.limitHit
            )
        }
    }

    @Test
    fun `GivenFavouritesVM WhenThereAreNoMoreEventsToLoad SameEventsAreReturned`() {
        testScope.runBlockingTest {
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList(initialEventsSize))
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

            // wait till initial loading completes
            viewModel.states.first { it.events.status is LoadedSuccessfully }

            // load more events
            val states: List<FavouritesState> = onPausedDispatcher {
                viewModel.send(LoadFavourites)
                viewModel.states
                    .take(3)
                    .toList()
            }

            // try load more after limit was hit
            viewModel.send(LoadFavourites)

            coVerify {
                getSavedEvents(initialEventsSize + FavouritesViewModel.limitIncrement)
                getSavedEvents(initialEventsSize + 2 * FavouritesViewModel.limitIncrement) wasNot called
            }
            val loadingMoreState = states[1]
            assert(
                loadingMoreState.events.status is Loading
                        && loadingMoreState.events.data.size == initialEventsSize
                        && loadingMoreState.limit == initialEventsSize
            )
            val finalState = states.last()
            assert(
                finalState.events.status is LoadedSuccessfully
                        && finalState.events.data.size == initialEventsSize
                        && finalState.limit == initialEventsSize
                        && finalState.limitHit
            )
        }
    }

    @Test
    fun `GivenFavouritesVM WhenThereAreMoreEventsToLoad MoreEventsAreReturned`() {
        testScope.runBlockingTest {
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(FavouritesViewModel.limitIncrement) } returns flowOf(
                    eventsList(initialEventsSize)
                )
                coEvery { this@mockk(FavouritesViewModel.limitIncrement + initialEventsSize) } returns flowOf(
                    eventsList(afterLoadMoreSize)
                )
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)

            // wait till initial loading completes
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)
            viewModel.states.first { it.events.status is LoadedSuccessfully }

            // load more events
            val states: List<FavouritesState> = onPausedDispatcher {
                viewModel.send(LoadFavourites)
                viewModel.states
                    .take(3)
                    .toList()
            }

            coVerify { getSavedEvents(initialEventsSize + FavouritesViewModel.limitIncrement) }
            val loadingMoreState = states[1]
            assert(
                loadingMoreState.events.status is Loading
                        && loadingMoreState.events.data.size == initialEventsSize
                        && loadingMoreState.limit == initialEventsSize
            )
            val finalState = states.last()
            assert(
                finalState.events.status is LoadedSuccessfully
                        && finalState.events.data.size == afterLoadMoreSize
                        && finalState.limit == afterLoadMoreSize
                        && !finalState.limitHit
            )
        }
    }
}