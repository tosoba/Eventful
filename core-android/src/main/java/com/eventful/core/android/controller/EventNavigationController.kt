package com.eventful.core.android.controller

import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.eventful.core.android.R
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.ext.castTo
import com.google.android.material.bottomnavigation.BottomNavigationView

interface EventNavigationController {
    val viewPager: ViewPager

    fun showEventDetails(event: Event)

    companion object {
        val navigationItems = mapOf(
            R.id.bottom_nav_event_details to 0,
            R.id.bottom_nav_weather to 1,
            R.id.bottom_nav_alarms to 2
        )
    }
}

val Fragment.eventNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener
    get() = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        EventNavigationController.navigationItems[item.itemId]?.let {
            parentFragment.castTo<EventNavigationController>()?.viewPager?.currentItem = it
            true
        } ?: false
    }
