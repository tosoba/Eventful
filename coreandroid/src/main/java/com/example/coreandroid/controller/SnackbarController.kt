package com.example.coreandroid.controller

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface SnackbarController {
    fun transitionToSnackbarState(newState: SnackbarState)
}

@ExperimentalCoroutinesApi
@FlowPreview
fun <T> T.handleSnackbarState(view: View): SendChannel<SnackbarState>
        where T : SnackbarController, T : LifecycleOwner {
    val snackbarStateChannel: BroadcastChannel<SnackbarState> = BroadcastChannel(
        capacity = Channel.CONFLATED
    )

    var snackbar: Snackbar? = null

    fun transitionTo(newState: SnackbarState) {
        snackbar?.dismiss()
        snackbar = when (newState) {
            is SnackbarState.Shown -> Snackbar.make(view, newState.text, newState.length).apply {
                newState.action?.let { setAction(it.msg, it.onClickListener) }
                newState.onDismissed?.let { onDismissed ->
                    addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) =
                            onDismissed()
                    })
                }
                show()
            }
            is SnackbarState.Hidden -> null
        }
    }

    snackbarStateChannel.asFlow()
        .distinctUntilChanged()
        .onEach { transitionTo(it) }
        .launchIn(lifecycleScope)

    return snackbarStateChannel
}

sealed class SnackbarState {
    data class Shown(
        val text: String,
        @Snackbar.Duration val length: Int = Snackbar.LENGTH_INDEFINITE,
        val action: SnackbarAction? = null,
        val onDismissed: (() -> Unit)? = null
    ) : SnackbarState()

    object Hidden : SnackbarState()
}

class SnackbarAction(val msg: String, val onClickListener: View.OnClickListener)
