package com.example.nearby

import com.example.coreandroid.arch.state.PagedAsyncData
import com.example.coreandroid.model.EventUiModel

data class NearbyState(
    val events: PagedAsyncData<EventUiModel>
) {
    companion object {
        val INITIAL = NearbyState(
            events = PagedAsyncData()
        )
    }
}