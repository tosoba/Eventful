package com.example.eventsnearby

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.coreandroid.base.BackStackNavigator
import com.example.coreandroid.base.DaggerViewModelActivity
import com.example.coreandroid.controller.DrawerLayoutController
import com.example.coreandroid.util.ext.hideBackNavArrow
import com.google.android.material.navigation.NavigationView
import com.markodevcic.peko.ActivityRotatingException
import com.markodevcic.peko.Peko
import com.markodevcic.peko.rationale.AlertDialogPermissionRationale
import com.markodevcic.peko.requestPermissionsAsync
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    BackStackNavigator,
    DrawerLayoutController,
    CoroutineScope {

    private val supervisorJob: CompletableDeferred<Any> = CompletableDeferred()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + supervisorJob

    override val drawerLayout: DrawerLayout? get() = main_drawer_layout
    private val drawerItemSelectedListener: NavigationView.OnNavigationItemSelectedListener =
        NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
            }
            main_drawer_layout.closeDrawer(GravityCompat.END)
            true
        }

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    internal lateinit var viewModelProvider: Provider<MainViewModel>
    private val viewModel: MainViewModel get() = viewModelProvider.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        supportFragmentManager.fragmentFactory = fragmentFactory
        super.onCreate(savedInstanceState)

        main_drawer_nav_view.setNavigationItemSelectedListener(drawerItemSelectedListener)

        if (savedInstanceState == null) {
            addFragment(MainFragment::class.java, false)
        }

        requestPermission()
    }

    override fun onDestroy() {
        if (isChangingConfigurations) supervisorJob.completeExceptionally(ActivityRotatingException())
        else supervisorJob.cancel()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean = onBackPressed().let { true }

    //TODO: this can also become an ext function
    private fun popBackStack(): Boolean {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if (backStackEntryCount >= 1) {
            supportFragmentManager.popBackStack()
            if (backStackEntryCount == 1) hideBackNavArrow()
            return true
        }
        return false
    }

    //TODO: convert to ext function
    //TODO: animator res as args
    override fun <T : Fragment> addFragment(
        fragmentClass: Class<T>,
        addToBackStack: Boolean,
        args: Bundle?
    ) {
        with(supportFragmentManager.beginTransaction()) {
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            add(R.id.main_navigation_view, fragmentClass, args)
            if (addToBackStack) addToBackStack(null)
            commit()
        }
    }

    override fun onBackPressed() {
        if (popBackStack()) return
        else super.onBackPressed()
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
