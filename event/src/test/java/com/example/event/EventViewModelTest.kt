package com.example.event

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSavedFlow
import com.example.core.usecase.SaveEvent
import com.example.core.util.*
import com.example.core.util.ext.takeWhileInclusive
import com.example.test.rule.event
import com.example.test.rule.onPausedDispatcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
        val isEventSavedFlow: IsEventSavedFlow = mockk {
            coEvery { this@mockk(any()) } returns flowOf(true)
        }

        val event = event()

        val states = onPausedDispatcher {
            EventViewModel(
                initialState = EventState(event,
                    Data(
                        false,
                        Initial
                    )
                ),
                isEventSaved = isEventSavedFlow,
                saveEvent = mockk(relaxed = true),
                deleteEvent = mockk(relaxed = true)
            ).states
                .takeWhileInclusive { it.isFavourite.status !is LoadedSuccessfully }
                .toList()
        }

        coVerify(exactly = 1) { isEventSavedFlow(event.id) }

        assert(states.size == 2)
        val initialState = states.first()
        assert(!initialState.isFavourite.data && initialState.isFavourite.status is Initial)
        val loadedState = states.last()
        assert(loadedState.isFavourite.data && loadedState.isFavourite.status is LoadedSuccessfully)
    }

    @Test
    fun `GivenEventVMWithSavedEvent WhenToggleFavourite DeleteEventIsCalled`() {
        testScope.runBlockingTest {
            val deleteEvent: DeleteEvent = mockk(relaxed = true)
            val event = event()
            val deletingEvent = CompletableDeferred<Unit>()
            val viewModel = EventViewModel(
                initialState = EventState(event,
                    Data(
                        false,
                        Initial
                    )
                ),
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
            launch {
                viewModel.states
                    .drop(1)
                    .takeWhileInclusive { it.isFavourite.data }.toList(states)
            }
            val signals = mutableListOf<EventSignal>()
            launch {
                viewModel.signals.take(1).toList(signals)
            }

            viewModel.intent(ToggleFavourite)
            deletingEvent.complete(Unit)

            coVerify(exactly = 1) { deleteEvent(event) }
            assert(states.size == 2)
            val loadingState = states.first()
            assert(loadingState.isFavourite.status is Loading && loadingState.isFavourite.data)
            val stateAfterSaving = states.last()
            assert(
                stateAfterSaving.isFavourite.status is LoadedSuccessfully
                        && !stateAfterSaving.isFavourite.data
            )
            assert(
                signals.size == 1
                        && signals.first() == EventSignal.FavouriteStateToggled(false)
            )
        }
    }

    @Test
    fun `GivenEventVMWithNonSavedEvent WhenToggleFavourite SaveEventIsCalled`() {
        testScope.runBlockingTest {
            val saveEvent: SaveEvent = mockk(relaxed = true)
            val event = event()
            val savingEvent = CompletableDeferred<Unit>()
            val viewModel = EventViewModel(
                initialState = EventState(event,
                    Data(
                        false,
                        Initial
                    )
                ),
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
            launch {
                viewModel.states
                    .drop(1)
                    .takeWhileInclusive { !it.isFavourite.data }
                    .toList(states)
            }
            val signals = mutableListOf<EventSignal>()
            launch {
                viewModel.signals.take(1).toList(signals)
            }
            viewModel.intent(ToggleFavourite)
            savingEvent.complete(Unit)

            coVerify(exactly = 1) { saveEvent(event) }
            assert(states.size == 2)
            val loadingState = states.first()
            assert(loadingState.isFavourite.status is Loading && !loadingState.isFavourite.data)
            val stateAfterSaving = states.last()
            assert(
                stateAfterSaving.isFavourite.status is LoadedSuccessfully
                        && stateAfterSaving.isFavourite.data
            )
            assert(
                signals.size == 1 &&
                        signals.first() == EventSignal.FavouriteStateToggled(true)
            )
        }
    }
}
