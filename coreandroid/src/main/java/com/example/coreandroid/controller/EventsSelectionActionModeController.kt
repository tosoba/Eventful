package com.example.coreandroid.controller

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.coreandroid.util.ext.plusAssign

interface EventsSelectionActionModeController {
    fun update(numberOfSelectedEvents: Int)
    fun finish(clearSelection: Boolean)
}

fun Fragment.eventsSelectionActionModeController(
    menuId: Int,
    itemClickedCallbacks: Map<Int, () -> Unit>,
    onDestroyActionMode: () -> Unit
): EventsSelectionActionModeController {
    var actionMode: ActionMode? = null
    val callback = ToolbarActionModeCallback(
        menuId,
        itemClickedCallbacks,
        onDestroyActionMode
    )
    val controller = object :
        EventsSelectionActionModeController {
        override fun update(numberOfSelectedEvents: Int) {
            if (actionMode == null && numberOfSelectedEvents > 0) {
                actionMode = activity?.startActionMode(callback)?.apply {
                    title = "$numberOfSelectedEvents selected"
                }
            } else if (actionMode != null) {
                if (numberOfSelectedEvents > 0)
                    actionMode?.title = "$numberOfSelectedEvents selected"
                else finish(false)
            }
        }

        override fun finish(clearSelection: Boolean) {
            callback.callOnDestroy = clearSelection
            actionMode?.finish()
            actionMode = null
        }
    }

    lifecycle += object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun finishOnPause() {
            controller.finish(false)
        }
    }

    return controller
}

private class ToolbarActionModeCallback(
    @MenuRes private val menuId: Int,
    private val itemClickedCallbacks: Map<Int, () -> Unit>,
    private val onDestroyActionMode: () -> Unit
) : ActionMode.Callback {

    var callOnDestroy = true

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

    override fun onDestroyActionMode(mode: ActionMode?) {
        if (callOnDestroy) onDestroyActionMode()
        else callOnDestroy = true
    }
}