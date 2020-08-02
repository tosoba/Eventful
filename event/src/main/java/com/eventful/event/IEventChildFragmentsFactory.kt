package com.eventful.event

import androidx.fragment.app.Fragment
import com.eventful.core.android.model.event.Event
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
interface IEventChildFragmentsFactory {
    fun eventDetailsFragment(event: Event, removeAlarmItem: Boolean): Fragment
    fun weatherFragment(latLng: LatLng?, locationName: String?, removeAlarmItem: Boolean): Fragment
    fun eventAlarmsFragment(event: Event): Fragment
}
