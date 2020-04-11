package com.example.favourites

import com.example.core.usecase.GetSavedEvents
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.util.Loading
import com.example.test.rule.MainDispatcherRule
import com.example.test.rule.getEvents
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@FlowPreview
internal class FavouritesVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private val initialEventsSize = 20
    private val afterLoadMoreSize = 30

    @Test
    fun `GivenFavouritesVM WhenInitialized ThenShouldGetSavedEvents`() {
        val getSavedEvents: GetSavedEvents = mockk(relaxed = true)
        FavouritesVM(getSavedEvents, testDispatcher)

        coVerify { getSavedEvents.invoke(any()) }
    }

    @Test
    fun `GivenFavouritesVM WhenInitializedAndGetSavedEventsReturnsSuccessfully ThenEventsAreStored`() =
        runBlocking {
            val getSavedEvents: GetSavedEvents = mockk {
                coEvery { this@mockk(FavouritesVM.limitIncrement) } returns flowOf(
                    getEvents(initialEventsSize)
                )
            }
            val viewModel = FavouritesVM(getSavedEvents, testDispatcher)

            val (savedEvents, limit) = viewModel.states.first { it.events.status is LoadedSuccessfully }
            assert(savedEvents.value.size == initialEventsSize && limit == initialEventsSize)
        }

    @Test
    fun `GivenFavouritesVM WhenThereAreNoMoreEventsToLoad SameEventsAreReturned`() = runBlocking {
        val getSavedEvents: GetSavedEvents = mockk {
            coEvery { this@mockk(FavouritesVM.limitIncrement) } returns flowOf(
                getEvents(initialEventsSize)
            )
            coEvery { this@mockk(FavouritesVM.limitIncrement + initialEventsSize) } returns flowOf(
                getEvents(initialEventsSize)
            )
        }
        // wait till initial loading completes
        val viewModel = FavouritesVM(getSavedEvents, testDispatcher)
        val first = viewModel.states.first { it.events.status is LoadedSuccessfully }

        val states = mutableListOf<FavouritesState>()
        val loadingJob = launch {
            viewModel.states.take(3).collect { states.add(it) }
        }

        // load more events
        viewModel.send(LoadFavourites)
        coVerify { getSavedEvents.invoke(any()) }
        loadingJob.join()

        val loadingState = states[1]
        assert(
            loadingState.events.status is Loading
                    && loadingState.events.value.size == initialEventsSize
                    && loadingState.limit == initialEventsSize
        )
        val finalState = states.last()
        assert(
            finalState.events.status is LoadedSuccessfully
                    && finalState.events.value.size == initialEventsSize
                    && finalState.limit == initialEventsSize
                    && finalState.limitHit
        )
    }
}