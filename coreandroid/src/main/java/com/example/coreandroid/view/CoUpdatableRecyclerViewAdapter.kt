package com.example.coreandroid.view

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

@ObsoleteCoroutinesApi
abstract class CoUpdatableRecyclerViewAdapter<T, VH : RecyclerView.ViewHolder>(
    initialItems: List<T> = Collections.emptyList(),
    diffUtilCallback: DiffUtil.ItemCallback<T>
) : RecyclerView.Adapter<VH>() {

    private val coDiffUtil: CoDiffUtil<T> by lazy { CoDiffUtil(this, diffUtilCallback) }

    init {
        if (initialItems.isNotEmpty()) update(initialItems)
    }

    override fun getItemCount(): Int = coDiffUtil.current.size

    @ObsoleteCoroutinesApi
    fun update(items: List<T>) = coDiffUtil.update(items)
}

