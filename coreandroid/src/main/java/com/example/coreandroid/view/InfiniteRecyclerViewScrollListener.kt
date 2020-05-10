package com.example.coreandroid.view

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@ExperimentalCoroutinesApi
class InfiniteRecyclerViewScrollListener(
    private val scrolledNearEndEventChannel: SendChannel<Unit>,
    private val threshold: Int = 3
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = view.layoutManager ?: return
        val itemCount = layoutManager.itemCount
        val lastVisibleItemPosition: Int = when (layoutManager) {
            is StaggeredGridLayoutManager -> layoutManager.findLastVisibleItemPositions(null).max()
                ?: 0
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            else -> 0
        }
        if (lastVisibleItemPosition + threshold > itemCount && !scrolledNearEndEventChannel.isClosedForSend) {
            scrolledNearEndEventChannel.offer(Unit)
        }
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
fun LifecycleOwner.infiniteRecyclerViewScrollListener(
    threshold: Int = 3,
    debounceTimeoutMillis: Long = 500,
    action: suspend (Unit) -> Unit
): InfiniteRecyclerViewScrollListener = InfiniteRecyclerViewScrollListener(
    scrolledNearEndEventChannel = Channel<Unit>().apply {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                close()
            }
        })

        if (!isClosedForReceive) {
            consumeAsFlow()
                .debounce(debounceTimeoutMillis)
                .onEach(action)
                .launchIn(lifecycleScope)
        }
    },
    threshold = threshold
)
