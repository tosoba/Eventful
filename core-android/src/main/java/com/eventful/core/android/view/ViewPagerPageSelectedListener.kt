package com.eventful.core.android.view

import androidx.viewpager.widget.ViewPager

interface ViewPagerPageSelectedListener : ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) = Unit
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) =
        Unit
}
