package com.example.nearby

import android.view.View
import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.Selectable
import com.example.core.model.event.IEvent
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.PagedDataList
import com.example.coreandroid.base.ClearSelectionUpdate
import com.example.coreandroid.base.ItemSelectionConfirmedUpdate
import com.example.coreandroid.base.StateUpdate
import com.example.coreandroid.base.ToggleItemSelectionUpdate
import com.example.coreandroid.controller.SnackbarAction
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.location.LocationStatus
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.haroldadmin.cnradapter.NetworkResponse

sealed class NearbyStateUpdate : StateUpdate<NearbyState> {
    data class ToggleEventSelection(
        override val item: Event
    ) : NearbyStateUpdate(),
        ToggleItemSelectionUpdate<NearbyState, Event, String> {
        override fun Event.id(): String = id
    }

    object ClearSelection : NearbyStateUpdate(), ClearSelectionUpdate<NearbyState, Event>

    object NoConnectionSnackbar : NearbyStateUpdate() {
        override fun invoke(state: NearbyState): NearbyState = NearbyState(
            snackbarState = SnackbarState.Shown("No connection.")
        )
    }

    data class LocationSnackbar(
        val latLng: LatLng?,
        val status: LocationStatus,
        val reloadLocation: () -> Unit
    ) : NearbyStateUpdate() {
        override fun invoke(state: NearbyState): NearbyState = when (status) {
            is LocationStatus.PermissionDenied -> state.copy(
                snackbarState = SnackbarState.Shown("No location permission.")
            )
            is LocationStatus.Disabled -> state.copy(
                snackbarState = SnackbarState.Shown("Location disabled.")
            )
            is LocationStatus.Loading -> state.copy(
                snackbarState = SnackbarState.Shown("Loading location...")
            )
            is LocationStatus.Error -> state.copy(
                snackbarState = SnackbarState.Shown(
                    "Unable to load location.",
                    length = if (latLng == null) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG,
                    action = SnackbarAction(
                        "Retry",
                        View.OnClickListener { reloadLocation() }
                    )
                )
            )
            else -> state
        }
    }

    object HideSnackbar : NearbyStateUpdate() {
        override fun invoke(state: NearbyState): NearbyState = state
            .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
    }

    sealed class Events : NearbyStateUpdate() {
        data class Loading(val newLocation: Boolean) : Events() {
            override fun invoke(state: NearbyState): NearbyState = state.copy(
                items = state.items.copyWithLoadingStatus,
                snackbarState = if (newLocation && state.items.data.isNotEmpty())
                    SnackbarState.Shown("Loading items in new location.")
                else state.snackbarState
            )
        }

        data class Loaded(
            val resource: Resource<PagedResult<IEvent>>,
            val clearEventsIfSuccess: Boolean
        ) : NearbyStateUpdate() {
            override fun invoke(state: NearbyState): NearbyState = state.run {
                when (resource) {
                    is Resource.Success -> copy(
                        items = if (clearEventsIfSuccess) PagedDataList(
                            resource.data.items.map { Selectable(Event(it)) },
                            status = LoadedSuccessfully,
                            offset = resource.data.currentPage + 1,
                            limit = resource.data.totalPages
                        ) else items.copyWithNewItems(
                            resource.data.items.map { Selectable(Event(it)) },
                            resource.data.currentPage + 1,
                            resource.data.totalPages
                        ),
                        snackbarState = SnackbarState.Hidden
                    )

                    is Resource.Error<PagedResult<IEvent>> -> copy(
                        items = items.copyWithFailureStatus(resource.error),
                        snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
                            if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504) {
                                SnackbarState.Shown("No connection.")
                            } else {
                                SnackbarState.Shown("Unknown network error.")
                            }
                        } else snackbarState
                    )
                }
            }
        }

        data class AddedToFavourites(
            override val snackbarText: String,
            override val onSnackbarDismissed: () -> Unit
        ) : NearbyStateUpdate(),
            ItemSelectionConfirmedUpdate<NearbyState, Event>
    }
}
