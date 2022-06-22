package com.eventful.nearby

import com.eventful.core.android.model.event.Event
import com.eventful.core.android.model.location.LocationState
import com.eventful.core.android.model.location.LocationStatus
import com.eventful.core.android.provider.ConnectedStateProvider
import com.eventful.core.android.provider.LocationStateProvider
import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.Selectable
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.util.Failure
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.util.PagedDataList
import com.eventful.test.relaxedMockedList
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class ConnectedStateProviderUpdatesTests : BaseNearbyFlowProcessorTests() {
    @Test
    @DisplayName("When not connected - should not call getPagedEvents")
    fun notConnectedTest() =
        testScope.runBlockingTest {
            val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)
            val connectedStateProvider =
                mockk<ConnectedStateProvider> { every { connectedStates } returns flowOf(false) }
            val currentState =
                mockk<() -> NearbyState> { every { this@mockk() } returns NearbyState() }

            flowProcessor(
                    getPagedEvents = getPagedEvents,
                    connectedStateProvider = connectedStateProvider)
                .updates(currentState = currentState)
                .launchIn(this)

            coVerify(exactly = 0) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
        }

    @Test
    @DisplayName("When events status is not Failed - should not call getPagedEvents")
    fun loadingNotFailedTest() {
        val currentState =
            mockk<() -> NearbyState> {
                every { this@mockk() } returns
                    NearbyState(items = PagedDataList(status = LoadedSuccessfully))
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
            mockk<() -> NearbyState> {
                every { this@mockk() } returns
                    NearbyState(
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
    fun allConditionsMetTest() =
        testScope.runBlockingTest {
            val currentState =
                mockk<() -> NearbyState> {
                    every { this@mockk() } returns
                        NearbyState(items = PagedDataList(status = Failure(null)))
                }
            val connectedStateProvider =
                mockk<ConnectedStateProvider> { every { connectedStates } returns flowOf(true) }
            val locationStateProvider =
                mockk<LocationStateProvider> {
                    every { locationStates } returns
                        flowOf(LocationState(LatLng(10.0, 10.0), LocationStatus.Found))
                }
            val getPagedEvents =
                mockk<GetPagedEvents> {
                    coEvery { this@mockk<Selectable<Event>>(any(), any(), any()) } returns
                        Resource.successWith(PagedResult(emptyList(), 0, 0))
                }

            flowProcessor(
                    getPagedEvents = getPagedEvents,
                    connectedStateProvider = connectedStateProvider,
                    locationStateProvider = locationStateProvider)
                .updates(currentState = currentState)
                .launchIn(testScope)

            coVerify(exactly = 1) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
        }
}
