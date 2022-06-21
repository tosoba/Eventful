package com.eventful

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.eventful.core.android.base.DaggerViewModelActivity
import com.eventful.core.android.controller.DrawerLayoutController
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.markodevcic.peko.ActivityRotatingException
import com.markodevcic.peko.Peko
import com.markodevcic.peko.rationale.AlertDialogPermissionRationale
import com.markodevcic.peko.requestPermissionsAsync
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    override val drawerLayout: DrawerLayout get() = binding.mainDrawerLayout
    private val drawerItemSelectedListener = NavigationView.OnNavigationItemSelectedListener {
        binding.mainDrawerLayout.closeDrawer(GravityCompat.END).let { false }
    }

    private val navigationFragment: MainNavigationFragment? by lazy(LazyThreadSafetyMode.NONE) {
        supportFragmentManager.findFragmentById(R.id.main_navigation_fragment) as? MainNavigationFragment
    }

    @Inject
    lateinit var navDestinations: IMainNavDestinations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.mainDrawerNavView.setNavigationItemSelectedListener(drawerItemSelectedListener)

        viewModel.viewUpdates
            .filterIsInstance<MainViewUpdate.DrawerMenu>()
            .onEach { (alarms, events) ->
                val drawerMenu = binding.mainDrawerNavView.menu
                val items = drawerMenu.children.map { it.itemId to it }.toMap()
                items[R.id.drawer_alarms]?.subMenu?.let { alarmsMenu ->
                    alarmsMenu.clear()
                    alarms.forEach { alarm ->
                        alarmsMenu.add(alarm.event.name).setOnMenuItemClickListener {
                            showEvent(alarm.event).let { false }
                        }
                    }
                }
                items[R.id.drawer_events]?.subMenu?.let { eventsMenu ->
                    eventsMenu.clear()
                    events.forEach { event ->
                        eventsMenu.add(event.name).setOnMenuItemClickListener {
                            showEvent(event).let { false }
                        }
                    }
                }
            }
            .launchIn(lifecycleScope)

        val drawerHeaderView = binding.mainDrawerNavView.getHeaderView(0)
        var drawerInsetsFixed = false
        ViewCompat.setOnApplyWindowInsetsListener(drawerHeaderView) { view, insets ->
            if (drawerInsetsFixed) return@setOnApplyWindowInsetsListener insets
            drawerInsetsFixed = true
            view.setPadding(
                view.paddingLeft,
                view.paddingTop + insets.systemWindowInsetTop,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        requireNotNull(navigationFragment)
            .backStackSignals
            .filterNot { it }
            .onEach { super.onBackPressed() }
            .launchIn(this)

        requestPermission()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.eventExtra?.let(::showEvent)
    }

    private fun showEvent(event: Event) {
        navigationFragment?.showEvent(event)
    }

    override fun onDestroy() {
        if (isChangingConfigurations) supervisorJob.completeExceptionally(ActivityRotatingException())
        else supervisorJob.cancel()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean = onBackPressed().let { true }

    override fun onBackPressed() {
        navigationFragment?.handleBackPressedOrPopBackStack() ?: super.onBackPressed()
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
