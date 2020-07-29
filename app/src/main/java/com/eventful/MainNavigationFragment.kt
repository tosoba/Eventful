package com.eventful

import androidx.fragment.app.Fragment
import com.eventful.core.android.base.BaseNavigationFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class MainNavigationFragment : BaseNavigationFragment() {
    override val navigationFragmentLayoutId: Int = R.layout.fragment_main_navigation
    override val backStackNavigationContainerId: Int = R.id.main_navigation_container_layout
    override val initialFragment: Fragment get() = MainFragment()
}
