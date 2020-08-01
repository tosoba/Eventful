package com.eventful.core.android.controller

import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.eventful.core.android.R
import com.eventful.core.android.util.ext.castTo
import com.eventful.core.android.view.ViewPagerPageSelectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

interface EventNavigationController {
    val viewPager: ViewPager

    companion object {
        val navigationItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
            put(R.id.bottom_nav_event_details, 0)
            put(R.id.bottom_nav_weather, 1)
            put(R.id.bottom_nav_alarms, 2)
        }

        fun onPageSelectedListenerWith(
            bottomNavigationView: BottomNavigationView?
        ): ViewPagerPageSelectedListener = object : ViewPagerPageSelectedListener {
            override fun onPageSelected(position: Int) {
                bottomNavigationView?.selectedItemId = inversedNavigationItems[position]
                    ?: error("Navigation item for position: $position is missing.")
            }
        }

        private val inversedNavigationItems: BiMap<Int, Int> = navigationItems.inverse()
    }
}

fun Fragment.addOnEventPageChangeListener(listener: ViewPager.OnPageChangeListener) {
    parentFragment.castTo<EventNavigationController>()
        ?.viewPager
        ?.addOnPageChangeListener(listener)
}

fun Fragment.removeOnEventPageChangeListener(listener: ViewPager.OnPageChangeListener) {
    parentFragment.castTo<EventNavigationController>()
        ?.viewPager
        ?.removeOnPageChangeListener(listener)
}

val Fragment.eventNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener
    get() = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        EventNavigationController.navigationItems[item.itemId]?.let {
            parentFragment.castTo<EventNavigationController>()?.viewPager?.currentItem = it
            true
        } ?: false
    }
