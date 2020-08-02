package com.eventful

import androidx.fragment.app.Fragment
import com.eventful.alarms.AlarmsFragment
import com.eventful.alarms.AlarmsMode
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.navigation.IMainChildFragmentNavDestinations
import com.eventful.event.EventFragment
import com.eventful.event.IEventChildFragmentsFactory
import com.eventful.event.details.EventDetailsFragment
import com.eventful.weather.WeatherFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
object FragmentFactory :
    IMainChildFragmentNavDestinations,
    IMainFragmentNavDestinations,
    IEventChildFragmentsFactory {

    override fun eventFragment(event: Event): Fragment = EventFragment.new(event)

    override val alarmsFragment: AlarmsFragment get() = AlarmsFragment.new(AlarmsMode.All)

    override fun eventDetailsFragment(
        event: Event,
        removeAlarmItem: Boolean
    ): EventDetailsFragment = EventDetailsFragment.new(event, removeAlarmItem)

    override fun weatherFragment(
        latLng: LatLng?, locationName: String?, removeAlarmItem: Boolean
    ): WeatherFragment = WeatherFragment.new(latLng, locationName, removeAlarmItem)

    override fun eventAlarmsFragment(event: Event): AlarmsFragment = AlarmsFragment
        .new(AlarmsMode.SingleEvent(event))
}
