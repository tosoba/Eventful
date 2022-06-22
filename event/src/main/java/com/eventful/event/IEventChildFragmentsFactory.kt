package com.eventful.event

import androidx.fragment.app.Fragment
import com.eventful.core.android.model.event.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
interface IEventChildFragmentsFactory {
    fun eventDetailsFragment(event: Event, bottomNavItemsToRemove: IntArray): Fragment
    fun weatherFragment(event: Event, bottomNavItemsToRemove: IntArray): Fragment
    fun eventAlarmsFragment(event: Event, bottomNavItemsToRemove: IntArray): Fragment
}
