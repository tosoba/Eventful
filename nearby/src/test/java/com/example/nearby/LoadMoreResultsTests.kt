package com.example.nearby

import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.Loading
import com.example.core.util.PagedDataList
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.model.location.LocationState
import com.example.coreandroid.model.location.LocationStatus
import com.example.coreandroid.provider.LocationStateProvider
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@FlowPreview
@ExperimentalCoroutinesApi
internal class LoadMoreResultsTests : BaseNearbyFlowProcessorTests() {

    private fun updatesFlow(
        getNearbyEvents: GetNearbyEvents = mockk(relaxed = true),
        getPagedEventsFlow: GetPagedEventsFlow = mockk(relaxed = true),
        currentState: () -> NearbyState,
        locationStateProvider: LocationStateProvider = mockk {
            every { locationStates } returns flowOf(
                LocationState(LatLng(10.0, 10.0), LocationStatus.Initial)
            )
        },
        signal: suspend (NearbySignal) -> Unit = mockk(relaxed = true)
    ): Flow<NearbyStateUpdate> = flowProcessor(
        getNearbyEvents = getNearbyEvents,
        getPagedEventsFlow = getPagedEventsFlow,
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
|- should call getPagedEventsFlow, signal EventsLoadingFinished, emit Events.Loading and Loaded updates"""
    )
    fun allConditionsMetTest() = testScope.runBlockingTest {
        val initialState = NearbyState(
            PagedDataList(
                status = LoadedSuccessfully,
                data = mockedList(20) { Selectable(event(it)) },
                offset = 1,
                limit = 5
            )
        )
        val expectedResource = Resource.successWith(
            PagedResult<IEvent>(mockedList(10) { event(it) }, 1, 1)
        )
        val getPagedEventsFlow = mockk<GetPagedEventsFlow> {
            every { this@mockk(initialState.items, any(), any()) } returns flowOf(
                expectedResource
            )
        }
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            getPagedEventsFlow = getPagedEventsFlow,
            currentState = mockk { every { this@mockk() } returns initialState },
            signal = signal::invoke
        ).toList()

        verify(exactly = 1) { getPagedEventsFlow(initialState.items, any(), any()) }
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