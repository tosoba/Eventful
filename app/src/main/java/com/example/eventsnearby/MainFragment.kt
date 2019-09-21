package com.example.eventsnearby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.example.coreandroid.base.SnackbarController
import com.example.coreandroid.util.SnackbarState
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
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import javax.inject.Inject


class MainFragment : DaggerFragment(), SnackbarController {

    private val bottomNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            viewPagerItems[item.itemId]?.let {
                main_view_pager?.currentItem = it
                return@OnNavigationItemSelectedListener true
            }
            false
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

    private val mainViewPagerAdapter: PagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, arrayOf(
                "Nearby" to NearbyFragment(),
                "Search" to SearchFragment(),
                "Favourites" to FavouritesFragment()
            )
        )
    }

    private var snackbar: Snackbar? = null

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false).apply {
        setupToolbarWithDrawerToggle(main_toolbar)

        main_bottom_nav_view.setOnNavigationItemSelectedListener(
            bottomNavigationItemSelectedListener
        )

        main_view_pager.adapter = mainViewPagerAdapter
        main_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        main_view_pager.offscreenPageLimit = 2

        main_fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        transition(viewModel.currentState.snackbarState)
    }

    override fun transition(newState: SnackbarState) {
        main_fab?.let {
            if (newState == viewModel.currentState.snackbarState) return

            snackbar?.dismiss()
            viewModel.snackbarState = newState
            when (newState) {
                is SnackbarState.Text -> {
                    snackbar = Snackbar.make(it, newState.text, Snackbar.LENGTH_INDEFINITE)
                        .apply(Snackbar::show)
                }
            }
        }
    }
}
