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
import kotlinx.coroutines.flow.*

interface SnackbarController {
    fun transitionToSnackbarState(newState: SnackbarState)
}

@ExperimentalCoroutinesApi
@FlowPreview
fun <T> T.handleSnackbarState(
    view: View
): SendChannel<SnackbarState> where T : SnackbarController, T : LifecycleOwner {
    val snackbarStateChannel: BroadcastChannel<SnackbarState> = BroadcastChannel(
        capacity = Channel.CONFLATED
    )

    var snackbar: Snackbar? = null

    fun transitionBetween(previousState: SnackbarState?, newState: SnackbarState) {
        when (newState) {
            is SnackbarState.Shown -> {
                if (snackbar != null
                    && snackbar?.isShown != false
                    && snackbar?.duration == Snackbar.LENGTH_INDEFINITE
                    && previousState is SnackbarState.Shown
                ) {
                    snackbar?.setText(newState.text)
                } else {
                    snackbar?.dismiss()
                    snackbar = Snackbar.make(view, newState.text, newState.length).apply {
                        newState.action?.let { setAction(it.msg, it.onClickListener) }
                        if (newState.onDismissed != null) {
                            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    newState.onDismissed.invoke()
                                }
                            })
                        }
                        show()
                    }
                }
            }
            is SnackbarState.Hidden -> {
                snackbar?.dismiss()
                snackbar = null
            }
        }
    }

    snackbarStateChannel.asFlow()
        .scan(Pair<SnackbarState?, SnackbarState?>(null, null)) { last2States, newState ->
            Pair(last2States.second, newState)
        }
        .drop(1)
        .onEach { (previous, new) ->
            if (previous != new) transitionBetween(previous, new!!)
        }
        .launchIn(lifecycleScope)

    return snackbarStateChannel
}

sealed class SnackbarState {
    data class Shown( //TODO: split Shown into Disimissable (indefinite) or non dismissable (short/long)
        val text: String,
        @Snackbar.Duration val length: Int = Snackbar.LENGTH_INDEFINITE,
        val action: SnackbarAction? = null,
        val onDismissed: (() -> Unit)? = null
    ) : SnackbarState()

    object Hidden : SnackbarState()
}

class SnackbarAction(val msg: String, val onClickListener: View.OnClickListener)
