package com.example.event

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSaved
import com.example.core.usecase.SaveEvent
import com.example.coreandroid.util.*
import com.example.test.rule.event
import com.example.test.rule.onPausedDispatcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
internal class EventViewModelTest {

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
    fun `GivenEventVM WhenInitialized IsFavouriteIsCalled`() = testScope.runBlockingTest {
        val isEventSaved: IsEventSaved = mockk {
            coEvery { this@mockk(any()) } returns flowOf(true)
        }

        val event = event()
        val (viewModel, states) = onPausedDispatcher {
            val eventViewModel = EventViewModel(
                initialState = EventState(event, Data(false, Initial)),
                isEventSaved = isEventSaved,
                saveEvent = mockk(relaxed = true),
                deleteEvent = mockk(relaxed = true)
            )
            eventViewModel to eventViewModel.states
                .takeWhileInclusive { it.isFavourite.status !is LoadedSuccessfully }
                .toList()
        }

        coVerify(exactly = 1) { isEventSaved(event.id) }

        assert(states.size == 2)
        val initialState = states.first()
        assert(!initialState.isFavourite.data && initialState.isFavourite.status is Initial)
        val loadedState = states.last()
        assert(loadedState.isFavourite.data && loadedState.isFavourite.status is LoadedSuccessfully)
        assert(viewModel.events.value == null)
    }

    @Test
    fun `GivenEventVMWithSavedEvent WhenToggleFavourite DeleteEventIsCalled`() {
        testScope.runBlockingTest {
            val deleteEvent: DeleteEvent = mockk(relaxed = true)
            val event = event()
            val deletingEvent = CompletableDeferred<Unit>()
            val viewModel = EventViewModel(
                initialState = EventState(event, Data(false, Initial)),
                isEventSaved = mockk {
                    coEvery { this@mockk(any()) } returns flow {
                        emit(true)
                        deletingEvent.await()
                        emit(false)
                    }
                },
                saveEvent = mockk(relaxed = true),
                deleteEvent = deleteEvent
            )

            val states = mutableListOf<EventState>()
            val job = launch {
                viewModel.states
                    .drop(1)
                    .takeWhileInclusive { it.isFavourite.data }.toList(states)
            }
            viewModel.send(ToggleFavourite)
            deletingEvent.complete(Unit)
            job.join()

            coVerify(exactly = 1) { deleteEvent(event) }

            assert(states.size == 2)
            val loadingState = states.first()
            assert(loadingState.isFavourite.status is Loading && loadingState.isFavourite.data)
            val stateAfterSaving = states.last()
            assert(
                stateAfterSaving.isFavourite.status is LoadedSuccessfully
                        && !stateAfterSaving.isFavourite.data
            )
            assert(viewModel.events.value == EventSignal.FavouriteStateToggled(false))
        }
    }

    @Test
    fun `GivenEventVMWithNonSavedEvent WhenToggleFavourite SaveEventIsCalled`() {
        testScope.runBlockingTest {
            val saveEvent: SaveEvent = mockk(relaxed = true)
            val event = event()
            val savingEvent = CompletableDeferred<Unit>()
            val viewModel = EventViewModel(
                initialState = EventState(event, Data(false, Initial)),
                isEventSaved = mockk {
                    coEvery { this@mockk(any()) } returns flow {
                        emit(false)
                        savingEvent.await()
                        emit(true)
                    }
                },
                saveEvent = saveEvent,
                deleteEvent = mockk(relaxed = true)
            )

            val states = mutableListOf<EventState>()
            val job = launch {
                viewModel.states
                    .drop(1)
                    .takeWhileInclusive { !it.isFavourite.data }.toList(states)
            }
            viewModel.send(ToggleFavourite)
            savingEvent.complete(Unit)
            job.join()

            coVerify(exactly = 1) { saveEvent(event) }

            assert(states.size == 2)
            val loadingState = states.first()
            assert(loadingState.isFavourite.status is Loading && !loadingState.isFavourite.data)
            val stateAfterSaving = states.last()
            assert(
                stateAfterSaving.isFavourite.status is LoadedSuccessfully
                        && stateAfterSaving.isFavourite.data
            )
            assert(viewModel.events.value == EventSignal.FavouriteStateToggled(true))
        }
    }
}
