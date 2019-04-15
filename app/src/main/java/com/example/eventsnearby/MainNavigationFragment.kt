package com.example.eventsnearby

import androidx.fragment.app.Fragment
import com.example.coreandroid.base.BaseNavigationFragment

class MainNavigationFragment : BaseNavigationFragment() {
    override val navigationFragmentLayoutId: Int = R.layout.fragment_main_navigation
    override val backStackNavigationContainerId: Int = R.id.main_navigation_container_layout
    override val initialFragment: Fragment get() = MainFragment()
}