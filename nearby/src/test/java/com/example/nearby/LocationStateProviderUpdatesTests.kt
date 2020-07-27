package com.example.nearby

import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.usecase.event.GetPagedEventsFlow
import com.example.core.util.Failure
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.PagedDataList
import com.example.coreandroid.model.location.LocationState
import com.example.coreandroid.model.location.LocationStatus
import com.example.test.rule.event
import com.example.test.rule.mockedList
import com.example.test.rule.relaxedMockedList
import com.google.android.gms.maps.model.LatLng
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class LocationStateProviderUpdatesTests : BaseNearbyFlowProcessorTests() {

    @Test
    @DisplayName("When events status is Failed - should not call getPagedEventsFlow")
    fun loadingFailedTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            items = PagedDataList(status = Failure(null))
        )
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            locationStateProvider = mockk {
                every { locationStates } returns flowOf(
                    LocationState(LatLng(10.0, 10.0), LocationStatus.Found)
                )
            }
        ).updates(
            currentState = currentState
        ).launchIn(this)

        verify(exactly = 0) { getPagedEventsFlow(initialState.items, any(), any()) }
    }

    @Test
    @DisplayName("When location status is not found - should not call getPagedEventsFlow")
    fun locationStatusNotFoundTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            items = PagedDataList(
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
            locationStateProvider = mockk {
                every { locationStates } returns flowOf(
                    LocationState(LatLng(10.0, 10.0), LocationStatus.Loading)
                )
            }
        ).updates(
            currentState = currentState
        ).launchIn(this)

        verify(exactly = 0) { getPagedEventsFlow(initialState.items, any(), any()) }
    }

    @Test
    @DisplayName(
        """When connected and all loading conditions met 
|- should call getPagedEventsFlow, signal EventsLoadingFinished, emit Events.Loading and Loaded updates"""
    )
    fun allConditionsMetTest() = testScope.runBlockingTest {
        val initialState = NearbyState(items = PagedDataList(status = LoadedSuccessfully))
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val expectedResource = Resource.successWith(
            PagedResult<IEvent>(mockedList(10) { event(it) }, 1, 1)
        )
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk(initialState.items, any(), any()) } returns flowOf(
                expectedResource
            )
        }
        val signal = mockk<Signal>(relaxed = true)

        val updates = flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            locationStateProvider = mockk {
                every { locationStates } returns flowOf(
                    LocationState(LatLng(10.0, 10.0), LocationStatus.Found)
                )
            }
        ).updates(
            currentState = currentState,
            signal = signal::invoke
        ).toList()

        verify(exactly = 1) { getPagedEventsFlow(initialState.items, any(), any()) }
        coVerify(exactly = 1) { signal.invoke(NearbySignal.EventsLoadingFinished) }
        assert(updates.size == 2)
        val loadingUpdate = updates.first()
        assert(loadingUpdate is NearbyStateUpdate.Events.Loading && loadingUpdate.newLocation)
        val loadedUpdate = updates.last()
        assert(
            loadedUpdate is NearbyStateUpdate.Events.Loaded
                    && loadedUpdate.resource == expectedResource
                    && loadedUpdate.clearEventsIfSuccess
        )
    }

    @Test
    @DisplayName("On more than one equal consecutive latLng - should call getPagedEventsFlow only once")
    fun latLngDistinctTest() = testScope.runBlockingTest {
        val initialState = NearbyState(items = PagedDataList(status = LoadedSuccessfully))
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEventsFlow = mockk<GetPagedEventsFlow>(relaxed = true)
        val locationState = LocationState(LatLng(10.0, 10.0), LocationStatus.Found)

        flowProcessor(
            getPagedEventsFlow = getPagedEventsFlow,
            locationStateProvider = mockk {
                every { locationStates } returns flowOf(locationState, locationState)
            }
        ).updates(
            currentState = currentState
        ).launchIn(this)

        verify(exactly = 1) { getPagedEventsFlow(initialState.items, any(), any()) }
    }
}
