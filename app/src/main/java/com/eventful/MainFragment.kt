package com.eventful

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.eventful.core.android.controller.*
import com.eventful.core.android.util.delegate.bottomNavItemSelectedViewPagerListener
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.util.delegate.viewPagerPageSelectedBottomNavListener
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.ViewPagerPageSelectedListener
import com.eventful.core.android.view.titledFragmentsPagerAdapter
import com.eventful.databinding.FragmentMainBinding
import com.eventful.favourites.FavouritesFragment
import com.eventful.nearby.NearbyFragment
import com.eventful.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class MainFragment : DaggerFragment(R.layout.fragment_main), MenuController, SnackbarController {

    private val binding: FragmentMainBinding by viewBinding(FragmentMainBinding::bind)

    private val bottomNavItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener
            by bottomNavItemSelectedViewPagerListener(navigationItems) { binding.mainViewPager }

    private val viewPagerSwipedListener: ViewPagerPageSelectedListener
            by viewPagerPageSelectedBottomNavListener(navigationItems.inverse()) { binding.mainBottomNavView }

    private val mainViewPagerAdapter: PagerAdapter by titledFragmentsPagerAdapter {
        arrayOf(
            getString(R.string.nearby) to NearbyFragment(),
            getString(R.string.search) to SearchFragment(),
            getString(R.string.favourites) to FavouritesFragment()
        )
    }

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    @Inject
    internal lateinit var navDestinations: IMainNavDestinations

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            setupToolbar(mainToolbar)
            setupToolbarWithDrawerToggle(mainToolbar)

            mainBottomNavView.setOnNavigationItemSelectedListener(bottomNavItemSelectedListener)

            mainViewPager.adapter = mainViewPagerAdapter
            mainViewPager.addOnPageChangeListener(viewPagerSwipedListener)
            mainViewPager.offscreenPageLimit = mainViewPagerAdapter.count - 1

            mainFab.setOnClickListener {
                navigationFragment?.showFragment(navDestinations.alarmsFragment)
            }

            snackbarStateChannel = handleSnackbarState(mainFab)

            requireNotNull(navigationFragment).backStackSignals
                .filter { it }
                .onEach {
                    setupToolbar(mainToolbar)
                    activity?.statusBarColor = context?.themeColor(R.attr.colorPrimaryDark)
                }
                .launchIn(lifecycleScope)
        }
    }

    override fun onDestroyView() {
        snackbarStateChannel.close()
        super.onDestroyView()
    }

    override fun initializeMenu(menuRes: Int, inflater: MenuInflater, initialize: (Menu) -> Unit) {
        binding.mainActionMenuView.initializeMenu(menuRes, inflater, initialize)
    }

    override fun clearMenu() {
        binding.mainActionMenuView.menu?.clear()
    }

    override fun transitionToSnackbarState(newState: SnackbarState) {
        if (!snackbarStateChannel.isClosedForSend) snackbarStateChannel.offer(newState)
    }

    companion object {
        private val navigationItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
            put(R.id.bottom_nav_nearby, 0)
            put(R.id.bottom_nav_search, 1)
            put(R.id.bottom_nav_favourites, 2)
        }
    }
}
