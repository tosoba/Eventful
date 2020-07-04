package com.example.eventsnearby

import android.Manifest
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.coreandroid.base.DaggerViewModelActivity
import com.example.coreandroid.controller.DrawerLayoutController
import com.example.coreandroid.util.delegate.viewBinding
import com.example.eventsnearby.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.markodevcic.peko.ActivityRotatingException
import com.markodevcic.peko.Peko
import com.markodevcic.peko.rationale.AlertDialogPermissionRationale
import com.markodevcic.peko.requestPermissionsAsync
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity :
    DaggerViewModelActivity<MainViewModel>(),
    DrawerLayoutController,
    CoroutineScope {

    private val supervisorJob: CompletableDeferred<Any> = CompletableDeferred()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + supervisorJob

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    override val drawerLayout: DrawerLayout? get() = main_drawer_layout
    private val drawerItemSelectedListener: NavigationView.OnNavigationItemSelectedListener =
        NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
            }
            binding.mainDrawerLayout.closeDrawer(GravityCompat.END)
            true
        }

    private val mainNavigationFragment: MainNavigationFragment? by lazy(LazyThreadSafetyMode.NONE) {
        supportFragmentManager.findFragmentById(R.id.main_navigation_fragment) as? MainNavigationFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding.mainDrawerNavView.setNavigationItemSelectedListener(drawerItemSelectedListener)

        requestPermission()
    }

    override fun onDestroy() {
        if (isChangingConfigurations) supervisorJob.completeExceptionally(ActivityRotatingException())
        else supervisorJob.cancel()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean = onBackPressed().let { true }

    override fun onBackPressed() {
        if (mainNavigationFragment?.onBackPressed() == true) {
            launch { viewModel.signal(MainSignal.PopMainBackStackSignal) }
            return
        } else super.onBackPressed()
    }

    private fun requestPermission(): Job = launch {
        val (grantedPermissions) = if (Peko.isRequestInProgress()) {
            Peko.resumeRequest()
        } else {
            requestPermissionsAsync(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                rationale = AlertDialogPermissionRationale(this@MainActivity) {
                    setTitle(getString(R.string.location_permission_needed))
                    setMessage(getString(R.string.no_location_permission_warning))
                }
            )
        }
        viewModel.intent(
            if (Manifest.permission.ACCESS_COARSE_LOCATION in grantedPermissions) MainIntent.LoadLocation
            else MainIntent.PermissionDenied
        )
    }
}
