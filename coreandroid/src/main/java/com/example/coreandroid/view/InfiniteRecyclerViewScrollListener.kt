package com.example.coreandroid.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class InfiniteRecyclerViewScrollListener(
    private val visibleThreshold: Int = 3,
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0

    // True if we are still waiting for the last set of data to load.
    private var loading = true

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        val layoutManager = view.layoutManager ?: return
        val totalItemCount = layoutManager.itemCount

        if (totalItemCount < previousTotalItemCount) {
            previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) loading = true
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        val lastVisibleItemPosition: Int = when (layoutManager) {
            is StaggeredGridLayoutManager -> layoutManager.findLastVisibleItemPositions(null).max()
                ?: 0
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            else -> 0
        }
        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            loadMore()
            loading = true
        }
    }

    // Call this method whenever performing new searches
    fun resetState() {
        previousTotalItemCount = 0
        loading = true
    }

    fun onLoadingError() {
        loading = false
    }
}