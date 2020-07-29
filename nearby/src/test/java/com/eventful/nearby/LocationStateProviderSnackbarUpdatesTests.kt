package com.eventful.nearby

import com.eventful.core.android.model.location.LocationState
import com.eventful.core.android.model.location.LocationStatus
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@FlowPreview
@ExperimentalCoroutinesApi
internal class LocationStateProviderSnackbarUpdatesTests : BaseNearbyFlowProcessorTests() {

    private fun updatesFlow(
        locationStatus: LocationStatus,
        signal: Signal
    ): Flow<NearbyStateUpdate> = flowProcessor(
        locationStateProvider = mockk {
            every { locationStates } returns flowOf(LocationState(status = locationStatus))
        }
    ).updates(
        signal = signal::invoke
    )

    @Test
    @DisplayName("When location status is Initial - should not signal or emit any updates")
    fun locationStatusInitialTest() = testScope.runBlockingTest {
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            locationStatus = LocationStatus.Initial,
            signal = signal
        ).toList()

        coVerify(exactly = 0) { signal(any()) }
        assert(updates.isEmpty())
    }

    @Test
    @DisplayName("When location status is Found - should not signal or emit any updates")
    fun locationStatusFoundTest() = testScope.runBlockingTest {
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            locationStatus = LocationStatus.Found,
            signal = signal
        ).toList()

        coVerify(exactly = 0) { signal(any()) }
        assert(updates.isEmpty())
    }

    @Test
    @DisplayName("When location status is Loading - should emit LocationSnackbar update and not signal")
    fun locationStatusLoadingTest() = testScope.runBlockingTest {
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            locationStatus = LocationStatus.Loading,
            signal = signal
        ).toList()

        coVerify(exactly = 0) { signal(any()) }
        assert(updates.size == 1)
        val snackbarUpdate = updates.first()
        assert(
            snackbarUpdate is NearbyStateUpdate.LocationSnackbar
                    && snackbarUpdate.status == LocationStatus.Loading
        )
    }

    @Test
    @DisplayName("When location status is PermissionDenied - should signal and emit LocationSnackbar update")
    fun locationStatusPermissionDeniedTest() = testScope.runBlockingTest {
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            locationStatus = LocationStatus.PermissionDenied,
            signal = signal
        ).toList()

        coVerify(exactly = 1) { signal(NearbySignal.EventsLoadingFinished) }
        assert(updates.size == 1)
        val snackbarUpdate = updates.first()
        assert(
            snackbarUpdate is NearbyStateUpdate.LocationSnackbar
                    && snackbarUpdate.status == LocationStatus.PermissionDenied
        )
    }

    @Test
    @DisplayName("When location status is Disabled - should signal and emit LocationSnackbar update")
    fun locationStatusDisabledTest() = testScope.runBlockingTest {
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            locationStatus = LocationStatus.Disabled,
            signal = signal
        ).toList()

        coVerify(exactly = 1) { signal(NearbySignal.EventsLoadingFinished) }
        assert(updates.size == 1)
        val snackbarUpdate = updates.first()
        assert(
            snackbarUpdate is NearbyStateUpdate.LocationSnackbar
                    && snackbarUpdate.status == LocationStatus.Disabled
        )
    }

    @Test
    @DisplayName("When location status is Error - should signal and emit LocationSnackbar update")
    fun locationStatusErrorTest() = testScope.runBlockingTest {
        val signal = mockk<Signal>(relaxed = true)

        val updates = updatesFlow(
            locationStatus = LocationStatus.Error(Exception()),
            signal = signal
        ).toList()

        coVerify(exactly = 1) { signal(NearbySignal.EventsLoadingFinished) }
        assert(updates.size == 1)
        val snackbarUpdate = updates.first()
        assert(
            snackbarUpdate is NearbyStateUpdate.LocationSnackbar
                    && snackbarUpdate.status is LocationStatus.Error
        )
    }
}
