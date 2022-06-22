package com.eventful.core.android.util.delegate

import androidx.annotation.MainThread
import com.eventful.core.android.view.ViewPagerPageSelectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView

@MainThread
inline fun viewPagerPageSelectedBottomNavListener(
    navigationItems: Map<Int, Int>,
    crossinline bottomNavigationView: () -> BottomNavigationView
): Lazy<ViewPagerPageSelectedListener> =
    lazy(LazyThreadSafetyMode.NONE) {
        object : ViewPagerPageSelectedListener {
            override fun onPageSelected(position: Int) {
                bottomNavigationView().selectedItemId =
                    navigationItems[position]
                        ?: error("Navigation item for position: $position is missing.")
            }
        }
    }
