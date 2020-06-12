package com.example.coreandroid.util.ext

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.coreandroid.R
import com.example.coreandroid.base.BackStackNavigator
import com.example.coreandroid.controller.DrawerLayoutController
import com.example.coreandroid.controller.MenuController
import com.example.coreandroid.controller.SnackbarController
import com.example.coreandroid.view.ActionBarDrawerToggleEnd

val Fragment.appCompatActivity: AppCompatActivity?
    get() = activity as? AppCompatActivity

val Fragment.backStackNavigator: BackStackNavigator?
    get() = activity as? BackStackNavigator

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
    appCompatActivity?.setSupportActionBar(toolbar)
    appCompatActivity?.supportActionBar?.setDisplayShowTitleEnabled(false)
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
    appCompatActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    appCompatActivity?.supportActionBar?.setDisplayShowHomeEnabled(true)
}

fun AppCompatActivity.hideBackNavArrow() {
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    supportActionBar?.setDisplayShowHomeEnabled(false)
}
