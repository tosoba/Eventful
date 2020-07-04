package com.example.nearby

import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.util.Failure
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.PagedDataList
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.model.location.LocationState
import com.example.coreandroid.model.location.LocationStatus
import com.example.coreandroid.provider.LocationStateProvider
import com.example.test.rule.relaxedMockedList
import com.google.android.gms.maps.model.LatLng
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
internal class LocationStateProviderTests : BaseNearbyFlowProcessorTests() {
    private val locationStateProvider: LocationStateProvider
        get() = mockk {
            every { locationStates } returns flowOf(
                LocationState(LatLng(10.0, 10.0), LocationStatus.Found)
            )
        }

    @Test
    @DisplayName("When events status is Failed - should not call getPagedEventsFlow")
    fun loadingFailedTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            events = PagedDataList(status = Failure(null))
        )
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            locationStateProvider = locationStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(this)

        verify(exactly = 0) { getPagedEventsFlow(initialState.events, any(), any()) }
    }

    @Test
    @DisplayName("When events list is not empty - should not call getPagedEventsFlow")
    fun eventsNotEmptyTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            events = PagedDataList(
                status = LoadedSuccessfully,
                data = relaxedMockedList(10)
            )
        )
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            locationStateProvider = locationStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(this)

        verify(exactly = 0) { getPagedEventsFlow(initialState.events, any(), any()) }
    }

    @Test
    @DisplayName("When connected and all loading conditions met - should call getPagedEventsFlow")
    fun allConditionsMetTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            events = PagedDataList(status = LoadedSuccessfully)
        )
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk<Selectable<Event>>(any(), any(), any()) } returns emptyFlow()
        }

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            locationStateProvider = locationStateProvider
        ).updates(
            currentState = currentState
        ).launchIn(this)

        verify(exactly = 1) { getPagedEventsFlow(initialState.events, any(), any()) }
    }
}