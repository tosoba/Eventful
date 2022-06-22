package com.eventful.core.android.util.delegate

import androidx.annotation.MainThread
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView

@MainThread
inline fun bottomNavItemSelectedViewPagerListener(
    navigationItems: Map<Int, Int>,
    crossinline viewPager: () -> ViewPager
): Lazy<BottomNavigationView.OnNavigationItemSelectedListener> =
    lazy(LazyThreadSafetyMode.NONE) {
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            navigationItems[item.itemId]?.let {
                viewPager().currentItem = it
                true
            }
                ?: false
        }
    }
