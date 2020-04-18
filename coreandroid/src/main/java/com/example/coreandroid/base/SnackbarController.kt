package com.example.coreandroid.base

import android.view.View
import com.example.coreandroid.util.SnackbarState
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

@FlowPreview
@ExperimentalCoroutinesApi
fun <T> T.handleSnackbarState(
    view: View
): SendChannel<SnackbarState> where T : SnackbarController, T : InjectableFragment {
    val snackbarStateChannel: BroadcastChannel<SnackbarState> = BroadcastChannel(
        capacity = Channel.CONFLATED
    )

    var snackbar: Snackbar? = null

    fun transitionBetween(previousState: SnackbarState?, newState: SnackbarState) {
        when (newState) {
            is SnackbarState.Text -> {
                if (snackbar != null
                    && snackbar?.isShown != false
                    && snackbar?.duration == Snackbar.LENGTH_INDEFINITE
                    && previousState is SnackbarState.Text
                ) {
                    snackbar?.setText(newState.text)
                } else {
                    snackbar?.dismiss()
                    snackbar = Snackbar.make(view, newState.text, newState.length)
                        .apply(Snackbar::show)
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
        .onEach { states -> transitionBetween(states.first, states.second!!) }
        .launchIn(fragmentScope)

    return snackbarStateChannel
}