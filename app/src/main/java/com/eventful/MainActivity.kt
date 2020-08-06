package com.eventful

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.eventful.core.android.base.DaggerViewModelActivity
import com.eventful.core.android.controller.DrawerLayoutController
import com.eventful.core.android.controller.EventNavigationController
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.notification.AlarmNotifications
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.markodevcic.peko.ActivityRotatingException
import com.markodevcic.peko.Peko
import com.markodevcic.peko.rationale.AlertDialogPermissionRationale
import com.markodevcic.peko.requestPermissionsAsync
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import javax.inject.Inject
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

    private val navigationFragment: MainNavigationFragment? by lazy(LazyThreadSafetyMode.NONE) {
        supportFragmentManager.findFragmentById(R.id.main_navigation_fragment) as? MainNavigationFragment
    }

    @Inject
    lateinit var navDestinations: IMainNavDestinations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding.mainDrawerNavView.setNavigationItemSelectedListener(drawerItemSelectedListener)

        requestPermission()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.eventExtra?.let(::showEvent)
    }

    private val Intent.eventExtra: Event?
        get() = extras?.getParcelable(AlarmNotifications.EVENT_EXTRA)

    private fun showEvent(event: Event) {
        navigationFragment?.currentTopFragment?.let { topFragment ->
            if (topFragment is EventNavigationController) {
                topFragment.showEventDetails()
            } else {
                navigationFragment?.showFragment(navDestinations.eventFragment(event))
            }
        }
    }

    override fun onDestroy() {
        if (isChangingConfigurations) supervisorJob.completeExceptionally(ActivityRotatingException())
        else supervisorJob.cancel()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean = onBackPressed().let { true }

    override fun onBackPressed() {
        if (navigationFragment?.onBackPressed() == true) launch {
            viewModel.signal(MainSignal.PopMainBackStackSignal)
        } else {
            super.onBackPressed()
        }
    }

    private fun requestPermission(): Job = launch {
        val (grantedPermissions) = if (Peko.isRequestInProgress()) {
            Peko.resumeRequest()
        } else requestPermissionsAsync(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            rationale = AlertDialogPermissionRationale(this@MainActivity) {
                setTitle(getString(R.string.location_permission_needed))
                setMessage(getString(R.string.no_location_permission_warning))
            }
        )
        viewModel.intent(
            if (Manifest.permission.ACCESS_COARSE_LOCATION in grantedPermissions) MainIntent.LoadLocation
            else MainIntent.PermissionDenied
        )
    }
}
