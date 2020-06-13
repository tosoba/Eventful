package com.example.eventsnearby

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.example.coreandroid.controller.*
import com.example.coreandroid.util.ext.setupToolbar
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.coreandroid.view.binding.viewBinding
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
import kotlinx.coroutines.channels.SendChannel

@ExperimentalCoroutinesApi
@FlowPreview
class MainFragment : DaggerFragment(R.layout.fragment_main), MenuController, SnackbarController {

    private val binding: FragmentMainBinding by viewBinding(FragmentMainBinding::bind)

    private val bottomNavItemSelectedListener = BottomNavigationView
        .OnNavigationItemSelectedListener { item ->
            navigationItems[item.itemId]?.let {
                binding.mainViewPager.currentItem = it
                true
            } ?: false
        }

    private val viewPagerSwipedListener = object : ViewPagerPageSelectedListener {
        override fun onPageSelected(position: Int) {
            binding.mainBottomNavView.selectedItemId = navigationItems.inverse()[position]!!
        }
    }

    private val mainViewPagerAdapter: TitledFragmentsPagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager,
            arrayOf(
                getString(R.string.nearby) to NearbyFragment(),
                getString(R.string.search) to SearchFragment(),
                getString(R.string.favourites) to FavouritesFragment()
            )
        )
    }

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    override fun initializeMenu(menuRes: Int, inflater: MenuInflater, initialize: (Menu) -> Unit) {
        binding.mainActionMenuView.initializeMenu(menuRes, inflater, initialize)
    }

    override fun clearMenu() {
        binding.mainActionMenuView.menu?.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            setupToolbar(mainToolbar)
            setupToolbarWithDrawerToggle(mainToolbar)

            mainBottomNavView.setOnNavigationItemSelectedListener(bottomNavItemSelectedListener)

            mainViewPager.adapter = mainViewPagerAdapter
            mainViewPager.addOnPageChangeListener(viewPagerSwipedListener)
            mainViewPager.offscreenPageLimit = 2

            mainFab.setOnClickListener {}

            snackbarStateChannel = handleSnackbarState(mainFab)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbarStateChannel.close()
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
