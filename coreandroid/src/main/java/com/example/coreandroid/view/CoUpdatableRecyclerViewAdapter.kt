package com.example.coreandroid.view

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

@ObsoleteCoroutinesApi
abstract class CoUpdatableRecyclerViewAdapter<T, VH : RecyclerView.ViewHolder>(
    lifecycleOwner: LifecycleOwner,
    diffUtilCallback: DiffUtil.ItemCallback<T>,
    initialItems: List<T> = Collections.emptyList()
) : RecyclerView.Adapter<VH>() {

    protected val coDiffUtil: CoDiffUtil<T> by lazy { CoDiffUtil(lifecycleOwner, this, diffUtilCallback) }

    val currentItems: List<T> get() = coDiffUtil.current

    init {
        if (initialItems.isNotEmpty()) update(initialItems)
    }

    override fun getItemCount(): Int = coDiffUtil.current.size

    fun update(items: List<T>) = coDiffUtil.update(items)

    fun onRecreated(previousItems: List<T>) = coDiffUtil.insertOnEmpty(previousItems)
}

