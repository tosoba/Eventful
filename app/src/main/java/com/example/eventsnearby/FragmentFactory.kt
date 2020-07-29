package com.example.eventsnearby

import androidx.fragment.app.Fragment
import com.example.alarms.AlarmsFragment
import com.example.alarms.AlarmsMode
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.event.EventFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
object FragmentFactory : IFragmentFactory {
    override fun eventFragment(event: Event): Fragment = EventFragment.new(event)
    override fun alarmsFragment(event: Event?): Fragment = AlarmsFragment
        .new(if (event != null) AlarmsMode.SingleEvent(event) else AlarmsMode.All)
}
