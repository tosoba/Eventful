package com.eventful.nearby

import com.eventful.core.android.model.event.Event
import com.eventful.core.android.model.location.LocationState
import com.eventful.core.android.model.location.LocationStatus
import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.Selectable
import com.eventful.core.model.event.IEvent
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.util.Failure
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.util.PagedDataList
import com.eventful.test.event
import com.eventful.test.mockedList
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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class LocationStateProviderUpdatesTests : BaseNearbyFlowProcessorTests() {

    @Test
    @DisplayName("When events status is Failed - should not call getPagedEvents")
    fun loadingFailedTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            items = PagedDataList(status = Failure(null))
        )
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

        flowProcessor(
            getPagedEvents = getPagedEvents,
            locationStateProvider = mockk {
                every { locationStates } returns flowOf(
                    LocationState(LatLng(10.0, 10.0), LocationStatus.Found)
                )
            }
        ).updates(
            currentState = currentState
        ).launchIn(this)

        coVerify(exactly = 0) { getPagedEvents(initialState.items, any(), any()) }
    }

    @Test
    @DisplayName("When location status is not found - should not call getPagedEvents")
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
        val getPagedEvents = mockk<GetPagedEvents>(relaxed = true)

        flowProcessor(
            getPagedEvents = getPagedEvents,
            locationStateProvider = mockk {
                every { locationStates } returns flowOf(
                    LocationState(LatLng(10.0, 10.0), LocationStatus.Loading)
                )
            }
        ).updates(
            currentState = currentState
        ).launchIn(this)

        coVerify(exactly = 0) { getPagedEvents(initialState.items, any(), any()) }
    }

    @Test
    @DisplayName(
        """When connected and all loading conditions met 
|- should call getPagedEvents, signal EventsLoadingFinished, emit Events.Loading and Loaded updates"""
    )
    fun allConditionsMetTest() = testScope.runBlockingTest {
        val initialState = NearbyState(items = PagedDataList(status = LoadedSuccessfully))
        val currentState = mockk<() -> NearbyState> {
            every { this@mockk() } returns initialState
        }
        val expectedResource = Resource.successWith(
            PagedResult<IEvent>(mockedList(10) { event(it) }, 1, 1)
        )
        val getPagedEvents = mockk<GetPagedEvents> {
            coEvery { this@mockk<Selectable<Event>>(any(), any(), any()) } returns expectedResource
        }
        val signal = mockk<Signal>(relaxed = true)

        val updates = flowProcessor(
            getPagedEvents = getPagedEvents,
            locationStateProvider = mockk {
                every { locationStates } returns flowOf(
                    LocationState(LatLng(10.0, 10.0), LocationStatus.Found)
                )
            }
        ).updates(
            currentState = currentState,
            signal = signal::invoke
        ).toList()

        coVerify(exactly = 1) { getPagedEvents<Selectable<Event>>(any(), any(), any()) }
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
}
