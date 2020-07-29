package com.eventful

import androidx.fragment.app.Fragment
import com.eventful.alarms.AlarmsFragment
import com.eventful.alarms.AlarmsMode
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.navigation.IFragmentFactory
import com.eventful.event.EventFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
object FragmentFactory : IFragmentFactory {
    override fun eventFragment(event: Event): Fragment = EventFragment.new(event)
    override fun alarmsFragment(event: Event?): Fragment = AlarmsFragment
        .new(if (event != null) AlarmsMode.SingleEvent(event) else AlarmsMode.All)
}
