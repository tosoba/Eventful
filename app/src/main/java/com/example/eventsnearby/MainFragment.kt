package com.example.eventsnearby

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.example.coreandroid.controller.*
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.provider.PopBackStackSignalProvider
import com.example.coreandroid.util.delegate.bottomNavItemSelectedViewPagerListener
import com.example.coreandroid.util.delegate.viewBinding
import com.example.coreandroid.util.delegate.viewPagerPageSelectedBottomNavListener
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.coreandroid.view.titledFragmentsPagerAdapter
import com.example.eventsnearby.databinding.FragmentMainBinding
import com.example.favourites.FavouritesFragment
import com.example.nearby.NearbyFragment
import com.example.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
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
    internal lateinit var popBackStackSignalProvider: PopBackStackSignalProvider

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    private var popBackStackSignalProviderJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            setupToolbar(mainToolbar)
            setupToolbarWithDrawerToggle(mainToolbar)

            mainBottomNavView.setOnNavigationItemSelectedListener(bottomNavItemSelectedListener)

            mainViewPager.adapter = mainViewPagerAdapter
            mainViewPager.addOnPageChangeListener(viewPagerSwipedListener)
            mainViewPager.offscreenPageLimit = 2

            mainFab.setOnClickListener {
                navigationFragment?.showFragment(fragmentFactory.alarmsFragment(null))
            }

            snackbarStateChannel = handleSnackbarState(mainFab)

            popBackStackSignalProviderJob = popBackStackSignalProvider.popBackStackSignals
                .onEach {
                    setupToolbar(mainToolbar)
                    activity?.statusBarColor = context?.themeColor(R.attr.colorPrimaryDark)
                }
                .launchIn(lifecycleScope)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbarStateChannel.close()
        popBackStackSignalProviderJob?.cancel()
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
