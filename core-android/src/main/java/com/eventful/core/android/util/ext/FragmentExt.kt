package com.eventful.core.android.util.ext

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.eventful.core.android.R
import com.eventful.core.android.base.BaseNavigationFragment
import com.eventful.core.android.controller.DrawerLayoutController
import com.eventful.core.android.controller.EventNavigationController
import com.eventful.core.android.controller.MenuController
import com.eventful.core.android.controller.SnackbarController
import com.eventful.core.android.view.ActionBarDrawerToggleEndListener

private val Fragment.appCompatActivity: AppCompatActivity?
    get() = activity as? AppCompatActivity

val Fragment.navigationFragment: BaseNavigationFragment?
    get() = findAncestorFragmentOfType()

val Fragment.snackbarController: SnackbarController?
    get() = findAncestorFragmentOfType()

val Fragment.menuController: MenuController?
    get() = findAncestorFragmentOfType()

val Fragment.eventNavigationController: EventNavigationController?
    get() = findAncestorFragmentOfType()

private inline fun <reified T> Fragment.findAncestorFragmentOfType(): T? {
    var ancestorFragment = parentFragment
    while (ancestorFragment != null) {
        if (ancestorFragment is T) return ancestorFragment
        ancestorFragment = ancestorFragment.parentFragment
    }
    return null
}

fun Fragment.setupToolbar(toolbar: Toolbar) {
    appCompatActivity?.setSupportActionBar(toolbar)
    appCompatActivity?.supportActionBar?.setDisplayShowTitleEnabled(false)
}

fun Fragment.setupToolbarWithDrawerToggle(
    toolbar: Toolbar,
    @DrawableRes drawerToggleRes: Int? = null
) {
    val activityRef = activity
    if (activityRef != null && activityRef is DrawerLayoutController) {
        val toggleListener = ActionBarDrawerToggleEndListener(
            activityRef,
            activityRef.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close,
            drawerToggleRes
        )
        activityRef.drawerLayout.addDrawerListener(toggleListener)
        toggleListener.syncState()
    }
}

fun Fragment.showBackNavArrow() {
    appCompatActivity?.supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setDisplayShowHomeEnabled(true)
    }
}

fun Fragment.hideBackNavArrow() {
    appCompatActivity?.supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(false)
        setDisplayShowHomeEnabled(false)
    }
}
