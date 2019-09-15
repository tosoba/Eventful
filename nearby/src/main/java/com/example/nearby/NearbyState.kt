package com.example.nearby

import com.example.coreandroid.arch.state.PagedDataList
import com.example.coreandroid.ticketmaster.Event
import com.haroldadmin.vector.VectorState

data class NearbyState(
    val events: PagedDataList<Event>
) : VectorState {
    companion object {
        val INITIAL = NearbyState(events = PagedDataList())
    }
}