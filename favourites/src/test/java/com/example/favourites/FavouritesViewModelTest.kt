package com.example.favourites

import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.test.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal class FavouritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private val initialEventsSize = 20
    private val afterLoadMoreSize = 30

    private fun getEvents(size: Int): List<Event> = (1..size).map { mockk<Event>(relaxed = true) }

    @Test
    fun `GivenFavouritesVM WhenInitialized ThenShouldGetSavedEvents`() {
        val getSavedEvents: GetSavedEvents = mockk(relaxed = true)
        FavouritesViewModel(getSavedEvents, testDispatcher)
        coVerify { getSavedEvents.invoke(any()) }
    }

    @Test
    fun `GivenFavouritesVM WhenInitializedAndGetSavedEventsReturnsSuccessfully ThenEventsAreStored`() =
        runBlocking {
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk.invoke(FavouritesViewModel.limitIncrement) } returns flowOf(
                    getEvents(initialEventsSize)
                )
            }
            val viewModel = FavouritesViewModel(getSavedEvents, testDispatcher)
            val (savedEvents, limit) = viewModel.state.first { it.events.status is LoadedSuccessfully }
            assert(savedEvents.value.size == initialEventsSize && limit == initialEventsSize)
        }

    @Test
    fun `GivenFavouritesVM WhenThereAreNoMoreEventsToLoad SameEventsAreReturned`() = runBlocking {
        val getSavedEvents: GetSavedEvents = mockk {
            coEvery { this@mockk.invoke(FavouritesViewModel.limitIncrement) } returns flowOf(
                getEvents(initialEventsSize)
            )
            coEvery { this@mockk.invoke(FavouritesViewModel.limitIncrement + initialEventsSize) } returns flowOf(
                getEvents(initialEventsSize)
            )
        }
        // wait till initial loading completes
        val viewModel = FavouritesViewModel(getSavedEvents, testDispatcher)
        viewModel.state.first { it.events.status is LoadedSuccessfully }

        val states = mutableListOf<FavouritesState>()
        val loadingJob = launch {
            viewModel.state.take(2).collect { states.add(it) }
        }

        //load more events
        viewModel.loadMoreEvents()
        coVerify { getSavedEvents.invoke(any()) }
        loadingJob.join()

        val loadingState = states[0]
        assert(
            loadingState.events.status is Loading
                    && loadingState.events.value.size == initialEventsSize
                    && loadingState.limit == initialEventsSize
        )
        val loadedMoreSuccessfullyState = states.last()
        assert(
            loadedMoreSuccessfullyState.events.status is LoadedSuccessfully
                    && loadedMoreSuccessfullyState.events.value.size == initialEventsSize
                    && loadedMoreSuccessfullyState.limit == initialEventsSize
        )
    }

    @Test
    fun `GivenFavouritesVM WhenThereAreMoreEventsToLoad MoreEventsAreReturned`() = runBlocking {
        val getSavedEvents: GetSavedEvents = mockk {
            coEvery { this@mockk.invoke(FavouritesViewModel.limitIncrement) } returns flowOf(
                getEvents(initialEventsSize)
            )
            coEvery { this@mockk.invoke(FavouritesViewModel.limitIncrement + initialEventsSize) } returns flowOf(
                getEvents(afterLoadMoreSize)
            )
        }
        // wait till initial loading completes
        val viewModel = FavouritesViewModel(getSavedEvents, testDispatcher)
        viewModel.state.first { it.events.status is LoadedSuccessfully }

        val states = mutableListOf<FavouritesState>()
        val loadingJob = launch {
            viewModel.state.take(2).collect { states.add(it) }
        }

        //load more events
        viewModel.loadMoreEvents()
        coVerify { getSavedEvents.invoke(any()) }
        loadingJob.join()

        val loadingState = states[0]
        assert(
            loadingState.events.status is Loading
                    && loadingState.events.value.size == initialEventsSize
                    && loadingState.limit == initialEventsSize
        )
        val loadedMoreState = states.last()
        assert(
            loadedMoreState.events.status is LoadedSuccessfully
                    && loadedMoreState.events.value.size == afterLoadMoreSize
                    && loadedMoreState.limit == afterLoadMoreSize
        )
    }
}