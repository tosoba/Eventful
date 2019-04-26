package com.example.eventsnearby

import android.Manifest
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.coreandroid.base.DrawerLayoutHost
import com.example.coreandroid.base.LocationController
import com.example.coreandroid.lifecycle.ConnectivityObserver
import com.example.coreandroid.lifecycle.LocationAvailabilityObserver
import com.example.coreandroid.util.LocationState
import com.example.coreandroid.util.observe
import com.example.coreandroid.util.plusAssign
import com.google.android.material.navigation.NavigationView
import com.markodevcic.peko.ActivityRotatingException
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionRequestResult
import com.markodevcic.peko.rationale.AlertDialogPermissionRationale
import com.markodevcic.peko.requestPermissionsAsync
import com.shopify.livedataktx.map
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class MainActivity : DaggerAppCompatActivity(), DrawerLayoutHost, CoroutineScope, LocationController {

    private val supervisorJob = CompletableDeferred<Any>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisorJob

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

    private val locationAvailabilityObserver: LocationAvailabilityObserver by lazy(LazyThreadSafetyMode.NONE) {
        LocationAvailabilityObserver(this) {
            if (it &&
                (viewModel.viewStateStore.currentState.locationState is LocationState.Disabled ||
                        viewModel.viewStateStore.currentState.locationState is LocationState.Unknown)
            ) {
                viewModel.loadLocation()
            }
        }
    }

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_drawer_nav_view.setNavigationItemSelectedListener(drawerNavigationItemSelectedListener)

        lifecycle += ConnectivityObserver {
            viewModel.viewStateStore.dispatchStateTransition { copy(isConnected = it) }
        }

        lifecycle += locationAvailabilityObserver

        val currentLocationState = viewModel.viewStateStore.currentState.locationState
        if (currentLocationState == LocationState.Unknown || currentLocationState == LocationState.Loading) {
            requestPermission()
        }

        viewModel.viewStateStore.liveState.map { it!!.locationState }.observe(this) {
            if (it is LocationState.Disabled) {
                locationAvailabilityObserver.start()
            } else {
                locationAvailabilityObserver.stop()
            }
        }
    }

    override fun onDestroy() {
        if (isChangingConfigurations) {
            supervisorJob.completeExceptionally(ActivityRotatingException())
        } else {
            supervisorJob.cancel()
        }
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (mainNavigationFragment?.onBackPressed() == true) return
        else super.onBackPressed()
    }

    override fun requestPermission() {
        if (Peko.isRequestInProgress()) launch {
            onRequestPermissionsResult(result = Peko.resumeRequest())
        } else launch {
            onRequestPermissionsResult(
                result = requestPermissionsAsync(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    rationale = AlertDialogPermissionRationale(this@MainActivity) {
                        setTitle(getString(R.string.location_permission_needed))
                        setMessage(getString(R.string.no_location_permission_warning))
                    })
            )
        }
    }

    private fun onRequestPermissionsResult(result: PermissionRequestResult) {
        val (grantedPermissions) = result
        if (Manifest.permission.ACCESS_COARSE_LOCATION !in grantedPermissions) {
            viewModel.viewStateStore.dispatchStateTransition {
                copy(locationState = LocationState.PermissionDenied)
            }
        } else {
            viewModel.loadLocation()
        }
    }
}
