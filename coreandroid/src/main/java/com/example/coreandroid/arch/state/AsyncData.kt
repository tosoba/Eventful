package com.example.coreandroid.arch.state

sealed class AsyncData<out T> {

    open val data: T? = null

    abstract fun <R> map(f: (T) -> R): AsyncData<R>

    inline fun doIfSuccess(block: (T) -> Unit) {
        if (this is Success) {
            block(data)
        }
    }

    data class Success<out T>(override val data: T) : AsyncData<T>() {
        override fun <R> map(f: (T) -> R): AsyncData<R> = Success(f(data))
    }

    data class Error(val message: String) : AsyncData<Nothing>() {
        constructor(t: Throwable) : this(t.message ?: "Unknown error.")

        override fun <R> map(f: (Nothing) -> R): AsyncData<R> = this
    }

    object Loading : AsyncData<Nothing>() {
        override fun <R> map(f: (Nothing) -> R): AsyncData<R> = this
    }
}

data class PagedAsyncData<T>(
    val offset: Int = 0,
    val items: List<T> = emptyList(),
    val totalItems: Int = Integer.MAX_VALUE,
    val lastLoadingStatus: LoadingStatus = LoadingStatus.Idle
) {
    val withLoadingInProgress: PagedAsyncData<T>
        get() = PagedAsyncData(lastLoadingStatus = LoadingStatus.InProgress)

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

    fun copyWithError(throwable: Throwable): PagedAsyncData<T> = copy(
        lastLoadingStatus = LoadingStatus.CompletedWithError(throwable)
    )

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
        data class CompletedWithError(val throwable: Throwable) : LoadingStatus()
    }
}
