package com.eventful

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.eventful.core.android.base.BaseNavigationFragment
import com.eventful.core.android.controller.EventController
import com.eventful.core.android.controller.EventNavigationController
import com.eventful.core.android.model.event.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class MainNavigationFragment : BaseNavigationFragment(), EventNavigationController {
    override val navigationFragmentLayoutId: Int = R.layout.fragment_main_navigation
    override val backStackNavigationContainerId: Int = R.id.main_navigation_container_layout
    override val initialFragment: Fragment get() = MainFragment()

    @Inject
    lateinit var navDestinations: IMainNavDestinations

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) activity?.intent?.eventExtra?.let { event ->
            showFragment(navDestinations.eventFragment(event))
        }
    }

    override fun showEvent(event: Event) {
        currentTopFragment?.let { topFragment ->
            if (topFragment is EventController) topFragment.showEventDetails(event)
            else showFragment(navDestinations.eventFragment(event))
        }
    }
}
