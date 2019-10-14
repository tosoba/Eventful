package com.example.coreandroid.view

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes

class ToolbarActionModeCallback(
    @MenuRes private val menuId: Int,
    private val itemClickedCallbacks: Map<Int, () -> Unit>
) : ActionMode.Callback {

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        itemClickedCallbacks[item.itemId]?.invoke()
        return false
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(menuId, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        itemClickedCallbacks.keys.forEach {
            menu.findItem(it)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) = Unit
}