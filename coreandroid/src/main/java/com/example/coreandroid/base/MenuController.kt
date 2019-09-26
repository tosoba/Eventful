package com.example.coreandroid.base

import androidx.appcompat.widget.ActionMenuView
import androidx.fragment.app.Fragment

interface MenuController {
    val menuView: ActionMenuView?
    fun shouldSetHasOptionsMenu(fragment: Fragment): Boolean
}