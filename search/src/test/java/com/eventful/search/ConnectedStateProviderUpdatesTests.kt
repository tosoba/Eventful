package com.eventful.search

import com.eventful.core.usecase.event.GetPagedEventsFlow
import com.eventful.core.util.Failure
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.util.PagedDataList
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable
import com.eventful.core.android.provider.ConnectedStateProvider
import com.eventful.test.relaxedMockedList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@FlowPreview
internal class ConnectedStateProviderUpdatesTests : BaseSearchFlowProcessorTests() {

    @Test
    @DisplayName("When not connected - should not call getPagedEventsFlow")
    fun notConnectedTest() {
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState(
                items = PagedDataList(status = Failure(null))
            )
        }
        val connectedStateProvider = mockk<ConnectedStateProvider> {
            every { connectedStates } returns flowOf(false)
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            connectedStateProvider = connectedStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(testScope)

        verify(exactly = 0) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("When events status is not Failed - should not call getPagedEventsFlow")
    fun loadingNotFailedTest() {
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState(
                items = PagedDataList(status = LoadedSuccessfully)
            )
        }
        val connectedStateProvider = mockk<ConnectedStateProvider> {
            every { connectedStates } returns flowOf(true)
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            connectedStateProvider = connectedStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(testScope)

        verify(exactly = 0) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("When events list is not empty - should not call getPagedEventsFlow")
    fun eventsNotEmptyTest() {
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState(
                items = PagedDataList(status = LoadedSuccessfully, data = relaxedMockedList(
                    1
                )
                )
            )
        }
        val connectedStateProvider = mockk<ConnectedStateProvider> {
            every { connectedStates } returns flowOf(true)
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            connectedStateProvider = connectedStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(testScope)

        verify(exactly = 0) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("When connected and all loading conditions met - should call getPagedEventsFlow")
    fun allConditionsMetTest() {
        val currentState = mockk<() -> SearchState> {
            every { this@mockk() } returns SearchState(
                items = PagedDataList(status = Failure(null))
            )
        }
        val connectedStateProvider = mockk<ConnectedStateProvider> {
            every { connectedStates } returns flowOf(true)
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk<Selectable<Event>>(any(), any(), any()) } returns emptyFlow()
        }

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            connectedStateProvider = connectedStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(testScope)

        verify(exactly = 1) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }
}