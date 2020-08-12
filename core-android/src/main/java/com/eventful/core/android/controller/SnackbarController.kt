package com.eventful.core.android.controller

import android.view.View
import androidx.annotation.StringRes
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

    suspend fun transitionTo(newState: SnackbarState) {
        snackbar?.dismiss()
        snackbar = when (newState) {
            is SnackbarState.Shown -> Snackbar.make(
                view,
                if (newState.msg.args.isEmpty()) {
                    view.context.getString(newState.msg.res)
                } else {
                    view.context.getString(newState.msg.res, *newState.msg.args)
                },
                newState.length
            ).apply {
                newState.action?.let {
                    setAction(
                        view.context.getString(it.msgRes),
                        it.onClickListener
                    )
                }
                newState.onDismissed?.let { onDismissed ->
                    addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            onDismissed()
                        }
                    })
                }
                show()
            }
            is SnackbarState.Hidden -> null
        }
    }

    snackbarStateChannel.asFlow()
        .distinctUntilChanged()
        .onEach(::transitionTo)
        .launchIn(lifecycleScope)

    return snackbarStateChannel
}

sealed class SnackbarState {
    data class Shown(
        val msg: MsgRes,
        @Snackbar.Duration val length: Int = Snackbar.LENGTH_INDEFINITE,
        val action: SnackbarAction? = null,
        val onDismissed: (() -> Unit)? = null
    ) : SnackbarState() {

        constructor(
            @StringRes res: Int,
            @Snackbar.Duration length: Int = Snackbar.LENGTH_INDEFINITE,
            action: SnackbarAction? = null,
            onDismissed: (() -> Unit)? = null
        ) : this(MsgRes(res), length, action, onDismissed)

        data class MsgRes(@StringRes val res: Int, val args: Array<Any> = arrayOf()) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as MsgRes

                if (res != other.res) return false
                if (!args.contentEquals(other.args)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = res
                result = 31 * result + args.contentHashCode()
                return result
            }
        }
    }

    object Hidden : SnackbarState()
}

class SnackbarAction(@StringRes val msgRes: Int, val onClickListener: View.OnClickListener)
