package com.example.coreandroid.view

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

@ObsoleteCoroutinesApi
abstract class CoUpdatableRecyclerViewAdapter<T, VH : RecyclerView.ViewHolder>(
    diffUtilCallback: DiffUtil.ItemCallback<T>,
    initialItems: List<T> = Collections.emptyList()
) : RecyclerView.Adapter<VH>() {

    protected val coDiffUtil: CoDiffUtil<T> by lazy { CoDiffUtil(this, diffUtilCallback) }

    init {
        if (initialItems.isNotEmpty()) update(initialItems)
    }

    override fun getItemCount(): Int = coDiffUtil.current.size

    @ObsoleteCoroutinesApi
    fun update(items: List<T>) = coDiffUtil.update(items)
}

