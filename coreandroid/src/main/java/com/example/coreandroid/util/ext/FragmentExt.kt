package com.example.coreandroid.util.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.coreandroid.R
import com.example.coreandroid.base.BaseNavigationFragment
import com.example.coreandroid.controller.DrawerLayoutController
import com.example.coreandroid.controller.MenuController
import com.example.coreandroid.controller.SnackbarController
import com.example.coreandroid.view.ActionBarDrawerToggleEnd

val Fragment.appCompatActivity: AppCompatActivity
    get() = activity as AppCompatActivity

val Fragment.navigationFragment: BaseNavigationFragment?
    get() = findAncestorFragmentOfType()

val Fragment.snackbarController: SnackbarController?
    get() = findAncestorFragmentOfType()

val Fragment.menuController: MenuController?
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
    appCompatActivity.setSupportActionBar(toolbar)
    appCompatActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
}

fun Fragment.setupToolbarWithDrawerToggle(toolbar: Toolbar) {
    val activityRef = activity
    if (activityRef != null && activityRef is DrawerLayoutController && activityRef.drawerLayout != null) {
        val drawerLayout = (activityRef as DrawerLayoutController).drawerLayout!!
        ActionBarDrawerToggleEnd(
            activityRef,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ).run {
            drawerLayout.addDrawerListener(this)
            syncState()
        }
    }
}

fun Fragment.showBackNavArrow() {
    appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    appCompatActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
}

fun Fragment.hideBackNavArrow() {
    appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    appCompatActivity.supportActionBar?.setDisplayShowHomeEnabled(false)
}
