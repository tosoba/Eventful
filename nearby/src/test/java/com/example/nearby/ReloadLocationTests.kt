package com.example.nearby

import com.example.core.util.PagedDataList
import com.example.coreandroid.provider.LocationStateProvider
import com.example.test.rule.relaxedMockedList
import io.mockk.coVerify
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
    @DisplayName("When no events - should signal EventsLoadingFinished")
    fun reloadLocationWhenEventsEmptyTest() = testScope.runBlockingTest {
        val signal = mockk<Signal>(relaxed = true)

        flowProcessor()
            .updates(
                intents = flowOf(NearbyIntent.ReloadLocation),
                currentState = mockk { every { this@mockk() } returns NearbyState() },
                signal = signal::invoke
            )
            .launchIn(this)

        coVerify(exactly = 1) { signal.invoke(NearbySignal.EventsLoadingFinished) }
    }

    @Test
    @DisplayName("When events are loaded - should call reloadLocation")
    fun reloadLocationWhenEventsLoadedTest() = testScope.runBlockingTest {
        val locationStateProvider = mockk<LocationStateProvider>(relaxed = true)

        flowProcessor(locationStateProvider = locationStateProvider)
            .updates(
                intents = flowOf(NearbyIntent.ReloadLocation),
                currentState = mockk {
                    every { this@mockk() } returns NearbyState(
                        events = PagedDataList(data = relaxedMockedList(10))
                    )
                }
            )
            .launchIn(this)

        verify(exactly = 1) { locationStateProvider.reloadLocation() }
    }
}