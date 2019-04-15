package com.example.eventsnearby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.example.coreandroid.base.SnackbarController
import com.example.coreandroid.util.SnackbarContent
import com.example.coreandroid.util.setupToolbarWithDrawerToggle
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi


@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class MainFragment : Fragment(), SnackbarController {

    private val bottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
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

    private val mainViewPagerAdapter: FragmentPagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, arrayOf(
                "Nearby" to NearbyFragment(),
                "Search" to SearchFragment(),
                "Favourites" to FavouritesFragment()
            )
        )
    }

    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false).apply {
        setupToolbarWithDrawerToggle(main_toolbar)

        main_bottom_nav_view.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)

        main_fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        main_view_pager.adapter = mainViewPagerAdapter
        main_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        main_view_pager.offscreenPageLimit = 2
    }

    override fun showSnackbar(content: SnackbarContent) {
        when (content) {
            is SnackbarContent.Loading -> {
                Snackbar.make(main_fab, content.message, Snackbar.LENGTH_INDEFINITE)
            }
        }
    }

    override fun hideSnackbar() {
        snackbar?.dismiss()
    }
}
