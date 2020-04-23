package com.example.coreandroid.controller

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.coreandroid.util.ext.plusAssign

interface EventsSelectionActionModeController {
    fun update(numberOfSelectedEvents: Int)
    fun finish(destroy: Boolean)
}

fun Fragment.eventsSelectionActionModeController(
    menuId: Int,
    itemClickedCallbacks: Map<Int, () -> Unit>,
    onDestroyActionMode: () -> Unit
): EventsSelectionActionModeController {
    var destroyOnFinish = true
    var actionMode: ActionMode? = null
    val callback = object : ActionMode.Callback {
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
            if (destroyOnFinish) onDestroyActionMode()
            actionMode = null
            destroyOnFinish = true
        }
    }
    val controller = object : EventsSelectionActionModeController {
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

        override fun finish(destroy: Boolean) {
            destroyOnFinish = destroy
            actionMode?.finish()
            actionMode = null
        }
    }

    //TODO: replace this with DefaultLifecycleObserver
    lifecycle += object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun finishOnPause() {
            controller.finish(false)
        }
    }

    return controller
}

