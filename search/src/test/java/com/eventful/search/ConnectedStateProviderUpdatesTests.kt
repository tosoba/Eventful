package com.eventful.search

import com.eventful.core.android.model.event.Event
import com.eventful.core.android.provider.ConnectedStateProvider
import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.Selectable
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.util.Failure
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.util.PagedDataList
import com.eventful.test.relaxedMockedList
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@FlowPreview
internal class ConnectedStateProviderUpdatesTests : BaseSearchFlowProcessorTests() {

    @Test
    @DisplayName("When not connected - should not call getPagedEvents")
    fun notConnectedTest() {
        val currentState =
            mockk<() -> SearchState> {
                every { this@mockk() } returns
                    SearchState(items = PagedDataList(status = Failure(null)))
            }
        val connectedStateProvider =
            mockk<ConnectedStateProvider> { every { connectedStates } returns flowOf(false) }
        val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

        flowProcessor(
                getPagedEvents = getPagedEvents, connectedStateProvider = connectedStateProvider)
            .updates(currentState = currentState)
            .launchIn(testScope)

        coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("When events status is not Failed - should not call getPagedEvents")
    fun loadingNotFailedTest() {
        val currentState =
            mockk<() -> SearchState> {
                every { this@mockk() } returns
                    SearchState(items = PagedDataList(status = LoadedSuccessfully))
            }
        val connectedStateProvider =
            mockk<ConnectedStateProvider> { every { connectedStates } returns flowOf(true) }
        val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

        flowProcessor(
                getPagedEvents = getPagedEvents, connectedStateProvider = connectedStateProvider)
            .updates(currentState = currentState)
            .launchIn(testScope)

        coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("When events list is not empty - should not call getPagedEvents")
    fun eventsNotEmptyTest() {
        val currentState =
            mockk<() -> SearchState> {
                every { this@mockk() } returns
                    SearchState(
                        items =
                            PagedDataList(status = LoadedSuccessfully, data = relaxedMockedList(1)))
            }
        val connectedStateProvider =
            mockk<ConnectedStateProvider> { every { connectedStates } returns flowOf(true) }
        val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

        flowProcessor(
                getPagedEvents = getPagedEvents, connectedStateProvider = connectedStateProvider)
            .updates(currentState = currentState)
            .launchIn(testScope)

        coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("When connected and all loading conditions met - should call getPagedEvents")
    fun allConditionsMetTest() {
        val currentState =
            mockk<() -> SearchState> {
                every { this@mockk() } returns
                    SearchState(items = PagedDataList(status = Failure(null)))
            }
        val connectedStateProvider =
            mockk<ConnectedStateProvider> { every { connectedStates } returns flowOf(true) }
        val getPagedEvents =
            mockk<GetPagedEvents> {
                coEvery { this@mockk<Selectable<Event>>(any(), any(), any()) } returns
                    Resource.successWith(PagedResult(emptyList(), 0, 0))
            }

        flowProcessor(
                getPagedEvents = getPagedEvents, connectedStateProvider = connectedStateProvider)
            .updates(currentState = currentState)
            .launchIn(testScope)

        coVerify(exactly = 1) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
    }
}
