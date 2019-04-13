package com.example.coreandroid.arch.state

data class PagedAsyncData<T>(
    val offset: Int = 0,
    val items: List<T> = emptyList(),
    val totalItems: Int = Integer.MAX_VALUE,
    val lastLoadingStatus: LoadingStatus = LoadingStatus.Idle
) {
    val lastLoadingFailed: Boolean
        get() = lastLoadingStatus is LoadingStatus.CompletedWithError

    val emptyAndLastLoadingFailed: Boolean
        get() = lastLoadingFailed && items.isEmpty()

    val withLoadingInProgress: PagedAsyncData<T>
        get() = copy(lastLoadingStatus = LoadingStatus.InProgress)

    fun copyWithNewItems(
        newItems: List<T>,
        offset: Int
    ): PagedAsyncData<T> = copy(
        items = items + newItems,
        offset = offset,
        lastLoadingStatus = LoadingStatus.CompletedSuccessfully
    )

    fun copyWithNewItems(
        newItems: List<T>,
        offset: Int,
        totalItems: Int
    ): PagedAsyncData<T> = copy(
        items = items + newItems,
        offset = offset,
        lastLoadingStatus = LoadingStatus.CompletedSuccessfully,
        totalItems = totalItems
    )

    fun copyWithError(throwable: Throwable?): PagedAsyncData<T> = copy(
        lastLoadingStatus = LoadingStatus.CompletedWithError(throwable)
    )

    inline fun doIfEmptyAndLoadingNotInProgress(block: (PagedAsyncData<T>) -> Unit) {
        if (lastLoadingStatus !is LoadingStatus.InProgress && items.isEmpty()) {
            block(this)
        }
    }

    inline fun doIfLoadingNotInProgress(block: (PagedAsyncData<T>) -> Unit) {
        if (lastLoadingStatus !is LoadingStatus.InProgress) {
            block(this)
        }
    }

    inline fun doIfLastLoadingCompletedWithError(block: (PagedAsyncData<T>) -> Unit) {
        if (lastLoadingStatus is LoadingStatus.CompletedWithError) {
            block(this)
        }
    }

    inline fun doIfLastLoadingCompletedSuccessFully(block: (PagedAsyncData<T>) -> Unit) {
        if (lastLoadingStatus is LoadingStatus.CompletedSuccessfully) {
            block(this)
        }
    }

    sealed class LoadingStatus {
        object Idle : LoadingStatus()
        object InProgress : LoadingStatus()
        object CompletedSuccessfully : LoadingStatus()
        data class CompletedWithError(val throwable: Throwable?) : LoadingStatus()
    }
}