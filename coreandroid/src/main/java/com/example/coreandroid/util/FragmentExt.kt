package com.example.coreandroid.util

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.coreandroid.R
import com.example.coreandroid.base.BaseNavigationFragment
import com.example.coreandroid.base.DrawerLayoutHost
import com.example.coreandroid.view.ActionBarDrawerToggleEnd

val Fragment.appCompatActivity: AppCompatActivity
    get() = activity as AppCompatActivity

val Fragment.drawerLayoutHost: DrawerLayoutHost
    get() = activity as DrawerLayoutHost

val Fragment.navigationFragment: BaseNavigationFragment?
    get() {
        var ancestorFragment = parentFragment
        while (ancestorFragment != null) {
            if (ancestorFragment is BaseNavigationFragment) return ancestorFragment
            ancestorFragment = ancestorFragment.parentFragment
        }
        return null
    }

fun Fragment.setupToolbarWithDrawerToggle(toolbar: Toolbar) {
    appCompatActivity.setSupportActionBar(toolbar)

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