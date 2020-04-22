package com.example.nearby

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.PagedDataList
import com.haroldadmin.cnradapter.NetworkResponse
import java.util.*

data class NearbyState(
    val events: PagedDataList<Selectable<Event>> = PagedDataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
)

internal fun NearbyState.reduce(
    resource: Resource<PagedResult<IEvent>>
): NearbyState = when (resource) {
    is Resource.Success -> copy(
        events = events.copyWithNewItems(
            //TODO: make distinctBy work on (Paged)DataList (to prevent duplicates between pages)
            resource.data.items.map { Selectable(Event(it)) }
                .distinctBy { it.item.name.toLowerCase(Locale.getDefault()).trim() },
            resource.data.currentPage + 1,
            resource.data.totalPages
        ),
        snackbarState = SnackbarState.Hidden
    )

    is Resource.Error<PagedResult<IEvent>, *> -> copy(
        events = events.copyWithError(resource.error),
        snackbarState = if (resource.error is NetworkResponse.ServerError<*>) {
            if ((resource.error as NetworkResponse.ServerError<*>).code in 503..504)
                SnackbarState.Text("No connection")
            else SnackbarState.Text("Unknown network error")
        } else snackbarState
    )
}