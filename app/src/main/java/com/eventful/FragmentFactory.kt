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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
object FragmentFactory :
    IMainChildFragmentNavDestinations,
    IMainNavDestinations,
    IEventChildFragmentsFactory {

    override fun eventFragment(event: Event): Fragment = EventFragment.new(event)

    override val alarmsFragment: Fragment get() = AlarmsFragment.new(AlarmsMode.All, intArrayOf())

    override fun eventDetailsFragment(
        event: Event, bottomNavItemsToRemove: IntArray
    ): EventDetailsFragment = EventDetailsFragment.new(event, bottomNavItemsToRemove)

    override fun weatherFragment(
        event: Event, bottomNavItemsToRemove: IntArray
    ): WeatherFragment = WeatherFragment.new(event, bottomNavItemsToRemove)

    override fun eventAlarmsFragment(
        event: Event, bottomNavItemsToRemove: IntArray
    ): AlarmsFragment = AlarmsFragment
        .new(AlarmsMode.SingleEvent(event), bottomNavItemsToRemove)
}
