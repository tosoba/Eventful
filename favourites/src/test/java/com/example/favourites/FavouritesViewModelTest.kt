package com.example.favourites

import com.example.core.usecase.DeleteEvents
import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.coreandroid.util.takeWhileInclusive
import com.example.test.rule.eventsList
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@FlowPreview
internal class FavouritesViewModelTest {

    companion object {
        private const val initialEventsSize = 20
        private const val afterLoadMoreSize = 30
    }

    private lateinit var testDispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        testDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GivenFavouritesVM WhenInitializedAndGetSavedEventsReturnsSuccessfully ThenEventsAreStored`() =
        runBlocking {
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(any()) } returns flowOf(eventsList(initialEventsSize))
            }
            val deleteEvents: DeleteEvents = mockk(relaxed = true)
            val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

            val states = viewModel.states
                .takeWhileInclusive { it.events.status !is LoadedSuccessfully }
                .toList()

            coVerify { getSavedEvents(FavouritesViewModel.limitIncrement) }
            val loadingState = states[states.size - 2]
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

    @Test
    fun `GivenFavouritesVM WhenThereAreNoMoreEventsToLoad SameEventsAreReturned`() = runBlocking {
        val getSavedEvents: GetSavedEvents = mockk {
            coEvery { this@mockk(any()) } returns flowOf(eventsList(initialEventsSize))
        }
        val deleteEvents: DeleteEvents = mockk(relaxed = true)
        val viewModel = FavouritesViewModel(getSavedEvents, deleteEvents, testDispatcher)

        // wait till initial loading completes
        viewModel.states.first { it.events.status is LoadedSuccessfully }

        // load more events
        val states = mutableListOf<FavouritesState>()
        val loadingMoreJob = launch {
            viewModel.states.takeWhileInclusive { !it.limitHit }.toList(states)
        }
        viewModel.send(LoadFavourites)
        loadingMoreJob.join()

        // try load more after limit was hit
        viewModel.send(LoadFavourites)

        coVerify {
            getSavedEvents(initialEventsSize + FavouritesViewModel.limitIncrement)
            getSavedEvents(initialEventsSize + 2 * FavouritesViewModel.limitIncrement) wasNot called
        }
        val loadingMoreState = states[states.size - 2]
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

    @Test
    fun `GivenFavouritesVM WhenThereAreMoreEventsToLoad MoreEventsAreReturned`() = runBlocking {
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
        val states = mutableListOf<FavouritesState>()
        val loadingMoreJob = launch {
            viewModel.states.takeWhileInclusive { it.events.status !is LoadedSuccessfully }
                .toList(states)
        }
        viewModel.send(LoadFavourites)
        loadingMoreJob.join()

        coVerify { getSavedEvents(initialEventsSize + FavouritesViewModel.limitIncrement) }
        val loadingMoreState = states[states.size - 2]
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