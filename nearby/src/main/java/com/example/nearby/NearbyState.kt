package com.example.nearby

import com.example.coreandroid.arch.state.PagedAsyncData
import com.example.coreandroid.model.EventUiModel
import com.google.android.gms.maps.model.LatLng

data class NearbyState(
    val events: PagedAsyncData<EventUiModel>,
    val userLatLng: LatLng //TODO: replace with sealed class to represent states like permission not given/unknown/maybe retrieving/known
) {
    companion object {
        val INITIAL = NearbyState(
            events = PagedAsyncData(),
            userLatLng = LatLng(52.237049, 21.017532)
        )
    }
}