package com.example.eventsnearby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.fragment.app.Fragment
import com.example.coreandroid.base.MenuController
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


class MainFragment : DaggerFragment(), SnackbarController, MenuController {

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
            viewModel.selectedFragmentIndex = position
            updateSnackbar(position, viewModel.currentState.snackbarState.getValue(position))
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

    @Inject
    internal lateinit var viewModel: MainViewModel

    override val menuView: ActionMenuView? get() = main_action_menu_view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
                .setAction("Action", null)
                .show()
        }

        with(viewModel.currentState) {
            snackbarState[selectedFragmentIndex]?.let { updateSnackbar(selectedFragmentIndex, it) }
        }
    }

    override fun transitionTo(newState: SnackbarState, fragment: Fragment) {
        val stateIndex = when (fragment) {
            is NearbyFragment -> 0
            is SearchFragment -> 1
            is FavouritesFragment -> 2
            else -> return
        }

        if (stateIndex != viewModel.selectedFragmentIndex || newState == viewModel.currentState.snackbarState[stateIndex])
            return

        viewModel.updateSnackbarState(stateIndex, newState)
        updateSnackbar(stateIndex, newState)
    }

    private fun updateSnackbar(stateIndex: Int, snackbarState: SnackbarState) {
        main_fab?.let {
            when (snackbarState) {
                is SnackbarState.Text -> {
                    if (snackbar != null
                        && snackbar?.isShown != false
                        && snackbar?.duration == Snackbar.LENGTH_INDEFINITE
                        && viewModel.currentState.snackbarState[stateIndex] is SnackbarState.Text
                    ) {
                        snackbar?.setText(snackbarState.text)
                    } else {
                        snackbar?.dismiss()
                        snackbar = Snackbar.make(it, snackbarState.text, snackbarState.length)
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

    override fun shouldSetHasOptionsMenu(
        fragment: Fragment
    ): Boolean = viewModel.selectedFragmentIndex == when (fragment) {
        is NearbyFragment -> 0
        is SearchFragment -> 1
        is FavouritesFragment -> 2
        else -> -1
    }

    override fun showTitle() {
        app_name_text_view?.visibility = View.VISIBLE
    }

    override fun hideTitle() {
        app_name_text_view?.visibility = View.GONE
    }
}
