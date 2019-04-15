package com.example.eventsnearby

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.coreandroid.base.DrawerLayoutHost
import com.example.coreandroid.lifecycle.ConnectivityObserver
import com.example.coreandroid.util.plusAssign
import com.google.android.material.navigation.NavigationView
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), DrawerLayoutHost {

    override val drawerLayout: DrawerLayout? get() = main_drawer_layout

    private val drawerNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {

        }
        main_drawer_layout.closeDrawer(GravityCompat.END)
        true
    }

    private val mainNavigationFragment: MainNavigationFragment? by lazy(LazyThreadSafetyMode.NONE) {
        supportFragmentManager.findFragmentById(R.id.main_navigation_fragment) as? MainNavigationFragment
    }

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycle += ConnectivityObserver {
            viewModel.viewStateStore.dispatchStateTransition { copy(isConnected = it) }
        }
        main_drawer_nav_view.setNavigationItemSelectedListener(drawerNavigationItemSelectedListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (mainNavigationFragment?.onBackPressed() == true) return
        else super.onBackPressed()
    }
}
