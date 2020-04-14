package com.example.eventsnearby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coreandroid.base.InjectableFragment
import com.example.coreandroid.base.SnackbarController
import com.example.coreandroid.util.SnackbarState
import com.example.coreandroid.util.ext.setupToolbar
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.favourites.FavouritesFragment
import com.example.nearby.NearbyFragment
import com.example.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import javax.inject.Inject


class MainFragment : InjectableFragment(), SnackbarController {

    private val bottomNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            viewPagerItems[item.itemId]?.let {
                main_view_pager?.currentItem = it
                return@OnNavigationItemSelectedListener true
            } ?: false
        }

    private val viewPagerItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
        put(R.id.bottom_nav_nearby, 0)
        put(R.id.bottom_nav_search, 1)
        put(R.id.bottom_nav_favourites, 2)
    }

    private val viewPagerSwipedListener = object : ViewPagerPageSelectedListener {
        override fun onPageSelected(position: Int) {
            main_bottom_nav_view.selectedItemId = viewPagerItems.inverse()[position]!!
        }
    }

    private val mainViewPagerAdapter: TitledFragmentsPagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, listOf(
                "Nearby" to NearbyFragment(),
                "Search" to SearchFragment(),
                "Favourites" to FavouritesFragment()
            )
        )
    }

    private var snackbar: Snackbar? = null

//    private val snackbarStateChannel: ConflatedBroadcastChannel<SnackbarState> =
//        ConflatedBroadcastChannel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false).apply {
        setupToolbar(main_toolbar)
        setupToolbarWithDrawerToggle(main_toolbar)

        main_bottom_nav_view.setOnNavigationItemSelectedListener(
            bottomNavigationItemSelectedListener
        )

        main_view_pager.adapter = mainViewPagerAdapter
        main_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        main_view_pager.offscreenPageLimit = 2

        main_fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

//        snackbarStateChannel.asFlow()
//            .scan(Pair<SnackbarState?, SnackbarState?>(null, null)) { last2States, newState ->
//                Pair(last2States.second, newState)
//            }
//            .drop(1)
//            .onEach { states -> transitionBetween(states.first, states.second!!) }
//            .launchIn(fragmentScope)
    }

    override fun onPause() {
        super.onPause()
//        snackbarStateChannel.close()
    }

    override fun transitionTo(newState: SnackbarState) {
//        snackbarStateChannel.offer(newState)
    }

    private fun transitionBetween(previousState: SnackbarState?, newState: SnackbarState) {
        main_fab?.let {
            when (newState) {
                is SnackbarState.Text -> {
                    if (snackbar != null
                        && snackbar?.isShown != false
                        && snackbar?.duration == Snackbar.LENGTH_INDEFINITE
                        && previousState is SnackbarState.Text
                    ) {
                        snackbar?.setText(newState.text)
                    } else {
                        snackbar?.dismiss()
                        snackbar = Snackbar.make(it, newState.text, newState.length)
                            .apply(Snackbar::show)
                    }
                }
                is SnackbarState.Hidden -> {
                    snackbar?.dismiss()
                    snackbar = null
                }
            }
        }
    }
}
