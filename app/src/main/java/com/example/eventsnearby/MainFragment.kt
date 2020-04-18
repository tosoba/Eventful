package com.example.eventsnearby

import android.os.Bundle
import android.view.*
import com.example.coreandroid.base.*
import com.example.coreandroid.util.SnackbarState
import com.example.coreandroid.util.ext.setupToolbar
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.favourites.FavouritesFragment
import com.example.nearby.NearbyFragment
import com.example.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel

@ExperimentalCoroutinesApi
@FlowPreview
class MainFragment : InjectableFragment(), MenuController, SnackbarController {

    private val bottomNavItemSelectedListener = BottomNavigationView
        .OnNavigationItemSelectedListener { item ->
            navigationItems[item.itemId]?.let {
                main_view_pager?.currentItem = it
                true
            } ?: false
        }

    private val viewPagerSwipedListener = object : ViewPagerPageSelectedListener {
        override fun onPageSelected(position: Int) {
            main_bottom_nav_view.selectedItemId = navigationItems.inverse()[position]!!
        }
    }

    private val mainViewPagerAdapter: TitledFragmentsPagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, arrayOf(
                getString(R.string.nearby) to NearbyFragment(),
                getString(R.string.search) to SearchFragment(),
                getString(R.string.favourites) to FavouritesFragment()
            )
        )
    }

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    override fun initializeMenu(menuRes: Int, inflater: MenuInflater, initialize: (Menu) -> Unit) {
        main_action_menu_view?.initializeMenu(menuRes, inflater, initialize)
    }

    override fun clearMenu() {
        main_action_menu_view?.menu?.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false).apply {
        setupToolbar(main_toolbar)
        setupToolbarWithDrawerToggle(main_toolbar)

        main_bottom_nav_view.setOnNavigationItemSelectedListener(bottomNavItemSelectedListener)

        main_view_pager.adapter = mainViewPagerAdapter
        main_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        main_view_pager.offscreenPageLimit = 2

        main_fab.setOnClickListener {}

        snackbarStateChannel = handleSnackbarState(main_fab)
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
