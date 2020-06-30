package com.example.nearby

import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.util.Failure
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.PagedDataList
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.model.location.LocationState
import com.example.coreandroid.model.location.LocationStatus
import com.example.coreandroid.provider.ConnectedStateProvider
import com.example.coreandroid.provider.LocationStateProvider
import com.example.test.rule.relaxedMockedList
import com.google.android.gms.maps.model.LatLng
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class ConnectedStateProviderUpdatesTests : BaseNearbyFlowProcessorTests() {
    @Test
    @DisplayName("When not connected - should not call getPagedEventsFlow")
    fun notConnectedTest() = testScope.runBlockingTest {
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)
        val connectedStateProvider = mockk<ConnectedStateProvider> {
            every { connectedStates } returns flowOf(false)
        }
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns NearbyState()
        }

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            connectedStateProvider = connectedStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(this)

        verify(exactly = 0) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }

    @Test
    @DisplayName("When events status is not Failed - should not call getPagedEventsFlow")
    fun loadingNotFailedTest() {
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns NearbyState(
                events = PagedDataList(status = LoadedSuccessfully)
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
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns NearbyState(
                events = PagedDataList(status = LoadedSuccessfully, data = relaxedMockedList(1))
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
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns NearbyState(
                events = PagedDataList(status = Failure(null))
            )
        }
        val connectedStateProvider = mockk<ConnectedStateProvider> {
            every { connectedStates } returns flowOf(true)
        }
        val locationStateProvider = mockk<LocationStateProvider> {
            every { locationStates } returns flowOf(
                LocationState(LatLng(10.0, 10.0), LocationStatus.Found)
            )
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk<Selectable<Event>>(any(), any(), any()) } returns emptyFlow()
        }

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            connectedStateProvider = connectedStateProvider,
            locationStateProvider = locationStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(testScope)

        coVerify(exactly = 1) { getPagedEventsFlow<Selectable<Event>>(any(), any(), any()) }
    }
}