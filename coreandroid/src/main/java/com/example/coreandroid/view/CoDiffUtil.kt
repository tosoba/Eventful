package com.example.coreandroid.view

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import kotlin.coroutines.CoroutineContext

class CoDiffUtil<T>(
    lifecycleOwner: LifecycleOwner,
    private val itemCallback: DiffUtil.ItemCallback<T>,
    private val listUpdateCallback: ListUpdateCallback
) : CoroutineScope {

    private val job: Job = Job()

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() = job.cancel()
        })
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private sealed class UpdateListOperation {
        object Clear : UpdateListOperation()
        data class Update<T>(val newList: List<T>) : UpdateListOperation()
    }

    private class SimpleUpdateCallback(
        private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>
    ) : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyItemRangeRemoved(position, count)
        }
    }

    constructor(
        lifecycleOwner: LifecycleOwner,
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
        itemCallback: DiffUtil.ItemCallback<T>
    ) : this(lifecycleOwner, itemCallback, SimpleUpdateCallback(adapter))

    @ObsoleteCoroutinesApi
    @Suppress("UNCHECKED_CAST")
    private val updateActor = actor<UpdateListOperation>(Dispatchers.Main, CONFLATED) {
        consumeEach {
            if (!isActive) return@actor

            val oldList = list

            when (it) {
                UpdateListOperation.Clear -> {
                    if (oldList != null) {
                        clear(oldList.size)
                    }
                }
                is UpdateListOperation.Update<*> -> {
                    if (oldList == null) {
                        insert(it.newList as List<T>)
                    } else if (oldList != it.newList) {
                        val callback = diffUtilCallback(oldList, it.newList as List<T>, itemCallback)
                        calculateDiff(it.newList, callback)
                    }
                }
            }
        }
    }

    private var list: List<T>? = null
    private var readOnlyList: List<T> = emptyList()
    val current: List<T> get() = readOnlyList

    @ObsoleteCoroutinesApi
    fun update(newList: List<T>?) {
        if (newList == null) {
            updateActor.offer(UpdateListOperation.Clear)
        } else {
            updateActor.offer(UpdateListOperation.Update(newList))
        }
    }

    fun insertOnEmpty(items: List<T>) {
        list = items
        readOnlyList = Collections.unmodifiableList(items)
        listUpdateCallback.onInserted(0, items.size)
    }

    private suspend fun clear(count: Int) {
        withContext(Dispatchers.Main) {
            list = null
            readOnlyList = emptyList()
            listUpdateCallback.onRemoved(0, count)
        }
    }

    private suspend fun insert(newList: List<T>) {
        withContext(Dispatchers.Main) { insertOnEmpty(newList) }
    }

    private suspend fun calculateDiff(newList: List<T>, callback: DiffUtil.Callback) {
        withContext(Dispatchers.Default) {
            val result = DiffUtil.calculateDiff(callback)
            if (!coroutineContext.isActive) return@withContext
            dispatchResult(newList, result)
        }
    }

    private suspend fun dispatchResult(newList: List<T>, result: DiffUtil.DiffResult) {
        withContext(Dispatchers.Main) {
            list = newList
            readOnlyList = Collections.unmodifiableList(newList)
            result.dispatchUpdatesTo(listUpdateCallback)
        }
    }

    private fun diffUtilCallback(
        oldList: List<T>, newList: List<T>, callback: DiffUtil.ItemCallback<T>
    ) = object : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return if (oldItem != null && newItem != null) {
                callback.areItemsTheSame(oldItem, newItem)
            } else {
                oldItem == null && newItem == null
            }
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return if (oldItem != null && newItem != null) {
                callback.areContentsTheSame(oldItem, newItem)
            } else if (oldItem == null && newItem == null) {
                return true
            } else {
                throw AssertionError()
            }
        }
    }
}