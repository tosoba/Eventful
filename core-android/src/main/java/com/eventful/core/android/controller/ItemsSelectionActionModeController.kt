package com.eventful.core.android.controller

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.eventful.core.android.util.ext.plusAssign

interface ItemsSelectionActionModeController {
    fun update(numberOfSelectedItems: Int)
    fun finish(destroy: Boolean)
}

fun Fragment.itemsSelectionActionModeController(
    menuId: Int,
    itemClickedCallbacks: Map<Int, () -> Unit>,
    onDestroyActionMode: () -> Unit
): ItemsSelectionActionModeController {
    var destroyOnFinish = true
    var actionMode: ActionMode? = null
    val callback =
        object : ActionMode.Callback {
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
    val controller =
        object : ItemsSelectionActionModeController {
            override fun update(numberOfSelectedItems: Int) {
                if (actionMode == null && numberOfSelectedItems > 0) {
                    actionMode =
                        activity?.startActionMode(callback)?.apply {
                            title = "$numberOfSelectedItems selected"
                        }
                } else if (actionMode != null) {
                    if (numberOfSelectedItems > 0)
                        actionMode?.title = "$numberOfSelectedItems selected"
                    else finish(false)
                }
            }

            override fun finish(destroy: Boolean) {
                destroyOnFinish = destroy
                actionMode?.finish()
                actionMode = null
            }
        }

    lifecycle +=
        object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                controller.finish(false)
            }
        }

    return controller
}
