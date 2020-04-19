package com.example.coreandroid.controller

import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.MenuRes
import androidx.appcompat.widget.ActionMenuView

interface MenuController {
    fun initializeMenu(@MenuRes menuRes: Int, inflater: MenuInflater, initialize: (Menu) -> Unit)
    fun clearMenu()
}

fun ActionMenuView.initializeMenu(
    @MenuRes menuRes: Int,
    inflater: MenuInflater,
    initialize: (Menu) -> Unit
) {
    menu.clear()
    inflater.inflate(menuRes, menu)
    initialize(menu)
}