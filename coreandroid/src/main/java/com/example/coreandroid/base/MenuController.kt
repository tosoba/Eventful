package com.example.coreandroid.base

import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.MenuRes
import androidx.appcompat.widget.ActionMenuView

interface MenuController {
    val menuView: ActionMenuView?
}

fun MenuController.clearMenu() {
    menuView?.menu?.clear()
}

inline fun MenuController.initializeMenu(
    @MenuRes menuRes: Int,
    inflater: MenuInflater,
    initialize: (Menu) -> Unit
) {
    menuView?.let { menuView ->
        menuView.menu.clear()
        inflater.inflate(menuRes, menuView.menu)
        initialize(menuView.menu)
    }
}