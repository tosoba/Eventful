package com.example.coreandroid.util.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.coreandroid.R
import com.example.coreandroid.base.BaseNavigationFragment
import com.example.coreandroid.base.DrawerLayoutHost
import com.example.coreandroid.base.MenuController
import com.example.coreandroid.base.SnackbarController
import com.example.coreandroid.view.ActionBarDrawerToggleEnd

val Fragment.appCompatActivity: AppCompatActivity
    get() = activity as AppCompatActivity

val Fragment.drawerLayoutHost: DrawerLayoutHost
    get() = activity as DrawerLayoutHost

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

fun Fragment.setupToolbarWithDrawerToggle(toolbar: Toolbar) {
    appCompatActivity.setSupportActionBar(toolbar)
    appCompatActivity.supportActionBar?.setDisplayShowTitleEnabled(false)

    ActionBarDrawerToggleEnd(
        activity!!,
        drawerLayoutHost.drawerLayout!!,
        toolbar,
        R.string.navigation_drawer_open,
        R.string.navigation_drawer_close
    ).run {
        drawerLayoutHost.drawerLayout!!.addDrawerListener(this)
        syncState()
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