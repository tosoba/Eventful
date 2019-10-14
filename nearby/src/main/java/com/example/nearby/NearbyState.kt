package com.example.nearby

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.PagedDataList
import com.haroldadmin.vector.VectorState

data class NearbyState(
    val events: PagedDataList<Selectable<Event>>
) : VectorState {
    companion object {
        val INITIAL: NearbyState
            get() = NearbyState(events = PagedDataList())
    }
}