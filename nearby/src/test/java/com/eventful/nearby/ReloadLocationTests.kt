package com.eventful.nearby

import com.eventful.core.android.provider.LocationStateProvider
import com.eventful.core.util.PagedDataList
import com.eventful.test.relaxedMockedList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class ReloadLocationTests : BaseNearbyFlowProcessorTests() {

    @Test
    @DisplayName("When no events - should not call reloadLocation")
    fun reloadLocationWhenEventsEmptyTest() =
        testScope.runBlockingTest {
            val locationStateProvider = mockk<LocationStateProvider>(relaxed = true)

            flowProcessor(locationStateProvider = locationStateProvider)
                .updates(
                    intents = flowOf(NearbyIntent.ReloadLocation),
                    currentState = mockk { every { this@mockk() } returns NearbyState() })
                .launchIn(this)

            verify(exactly = 0) { locationStateProvider.reloadLocation() }
        }

    @Test
    @DisplayName("When events are loaded - should call reloadLocation")
    fun reloadLocationWhenEventsLoadedTest() =
        testScope.runBlockingTest {
            val locationStateProvider = mockk<LocationStateProvider>(relaxed = true)

            flowProcessor(locationStateProvider = locationStateProvider)
                .updates(
                    intents = flowOf(NearbyIntent.ReloadLocation),
                    currentState =
                        mockk {
                            every { this@mockk() } returns
                                NearbyState(items = PagedDataList(data = relaxedMockedList(10)))
                        })
                .launchIn(this)

            verify(exactly = 1) { locationStateProvider.reloadLocation() }
        }
}
