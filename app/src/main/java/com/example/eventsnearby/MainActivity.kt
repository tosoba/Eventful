package com.example.eventsnearby

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentPagerAdapter
import com.example.coreandroid.view.ActionBarDrawerToggleEnd
import com.example.favourites.FavouritesFragment
import com.example.nearby.NearbyFragment
import com.example.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

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

    private val drawerNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {

        }
        main_drawer_layout.closeDrawer(GravityCompat.END)
        true
    }

    private val mainViewPagerAdapter: FragmentPagerAdapter = TitledFragmentsPagerAdapter(
        supportFragmentManager, arrayOf(
            "Nearby" to NearbyFragment(),
            "Search" to SearchFragment(),
            "Favourites" to FavouritesFragment()
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)

        main_bottom_nav_view.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)

        main_fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        ActionBarDrawerToggleEnd(
            this, main_drawer_layout, main_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ).run {
            main_drawer_layout.addDrawerListener(this)
            syncState()
        }

        main_drawer_nav_view.setNavigationItemSelectedListener(drawerNavigationItemSelectedListener)

        main_view_pager.adapter = mainViewPagerAdapter
        main_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        main_view_pager.offscreenPageLimit = 2
    }
}
