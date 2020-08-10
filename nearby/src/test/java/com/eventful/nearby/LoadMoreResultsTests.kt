package com.eventful.nearby

import com.eventful.core.android.model.location.LocationState
import com.eventful.core.android.model.location.LocationStatus
import com.eventful.core.android.provider.LocationStateProvider
import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.Selectable
import com.eventful.core.model.event.IEvent
import com.eventful.core.usecase.event.GetNearbyEvents
import com.eventful.core.usecase.event.GetPagedEvents
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.util.Loading
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class LoadMoreResultsTests : BaseNearbyFlowProcessorTests() {

    private fun updatesFlow(
        getNearbyEvents: GetNearbyEvents = mockk(relaxed = true),
        getPagedEvents: GetPagedEvents = mockk(relaxed = true),
        currentState: () -> NearbyState,
        locationStateProvider: LocationStateProvider = mockk {
            every { locationStates } returns flowOf(
                LocationState(LatLng(10.0, 10.0), LocationStatus.Initial)
            )
        },
        signal: suspend (NearbySignal) -> Unit = mockk(relaxed = true)
    ): Flow<NearbyStateUpdate> = flowProcessor(
        getNearbyEvents = getNearbyEvents,
        getPagedEvents = getPagedEvents,
        locationStateProvider = locationStateProvider
    ).updates(
        currentState = currentState,
        intents = flowOf(NearbyIntent.LoadMoreResults),
        signal = signal
    )

    @Test
    @DisplayName("On LoadMoreResults when loading in progress - should not call getNearbyEvents")
    fun loadMoreResultsLoadingTest() = testScope.runBlockingTest {
        val getNearbyEvents = mockk<GetNearbyEvents>(relaxed = true)

        updatesFlow(
            getNearbyEvents = getNearbyEvents,
            currentState = mockk {
                every { this@mockk() } returns NearbyState(
                    PagedDataList(
                        status = Loading,
                        data = relaxedMockedList(20)
                    )
                )
            }
        ).launchIn(this)

        coVerify(exactly = 0) { getNearbyEvents(any(), any(), any()) }
    }

    @Test
    @DisplayName("On LoadMoreResults when events are empty - should not call getNearbyEvents")
    fun loadMoreResultsWhenEmptyTest() = testScope.runBlockingTest {
        val getNearbyEvents = mockk<GetNearbyEvents>(relaxed = true)

        updatesFlow(
            getNearbyEvents = getNearbyEvents,
            currentState = mockk {
                every { this@mockk() } returns NearbyState(PagedDataList(data = emptyList()))
            }
        ).launchIn(this)

        coVerify(exactly = 0) { getNearbyEvents(any(), any(), any()) }
    }

    @Test
    @DisplayName("On LoadMoreResults when limit hit - should not call getNearbyEvents")
    fun loadMoreResultsWhenEmptyTestWhenLimitHit() = testScope.runBlockingTest {
        val getNearbyEvents = mockk<GetNearbyEvents>(relaxed = true)

        updatesFlow(
            getNearbyEvents = getNearbyEvents,
            currentState = mockk {
                every { this@mockk() } returns NearbyState(
                    PagedDataList(
                        data = relaxedMockedList(20),
                        offset = 2,
                        limit = 1
                    )
                )
            }
        ).launchIn(this)

        coVerify(exactly = 0) { getNearbyEvents(any(), any(), any()) }
    }

    @Test
    @DisplayName(
        """When all loading met 
|- should call getPagedEvents, signal EventsLoadingFinished, emit Events.Loading and Loaded updates"""
    )
    fun allConditionsMetTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            PagedDataList(
                status = LoadedSuccessfully,
                data = mockedList(20) {
                    Selectable(
                        event(it)
                    )
                },
                offset = 1,
                limit = 5
            )
        )
        val expectedResource = Resource.successWith(
            PagedResult<IEvent>(mockedList(10) { event(it) }, 1, 1)
        )
        val getPagedEvents = mockk<GetPagedEvents> {
            coEvery { this@mockk(initialState.items, any(), any()) } returns expectedResource
        }
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            getPagedEvents = getPagedEvents,
            currentState = mockk { every { this@mockk() } returns initialState },
            signal = signal::invoke
        ).toList()

        coVerify(exactly = 1) { getPagedEvents(initialState.items, any(), any()) }
        coVerify(exactly = 1) { signal.invoke(NearbySignal.EventsLoadingFinished) }
        assert(updates.size == 2)
        val loadingUpdate = updates.first()
        assert(loadingUpdate is NearbyStateUpdate.Events.Loading && !loadingUpdate.newLocation)
        val loadedUpdate = updates.last()
        assert(
            loadedUpdate is NearbyStateUpdate.Events.Loaded
                    && loadedUpdate.resource == expectedResource
                    && !loadedUpdate.clearEventsIfSuccess
        )
    }
}