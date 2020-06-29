package com.example.coreandroid.util.ext

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.coreandroid.R
import com.example.coreandroid.base.BaseNavigationFragment
import com.example.coreandroid.controller.DrawerLayoutController
import com.example.coreandroid.controller.MenuController
import com.example.coreandroid.controller.SnackbarController

private val Fragment.appCompatActivity: AppCompatActivity?
    get() = activity as? AppCompatActivity

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
    appCompatActivity?.setSupportActionBar(toolbar)
    appCompatActivity?.supportActionBar?.setDisplayShowTitleEnabled(false)
}

fun Fragment.setupToolbarWithDrawerToggle(toolbar: Toolbar) {
    val activityRef = activity
    if (activityRef != null && activityRef is DrawerLayoutController) {
        activityRef.drawerLayout?.let {
            ActionBarDrawerToggle(
                activityRef,
                it,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            ).run {
                it.addDrawerListener(this)
                syncState()
            }
        }
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
