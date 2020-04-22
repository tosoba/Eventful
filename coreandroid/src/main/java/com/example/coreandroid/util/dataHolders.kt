package com.example.coreandroid.util

interface HoldsData<T> {
    val data: T
    val status: DataStatus
    val copyWithLoadingInProgress: HoldsData<T>
    val loadingFailed: Boolean get() = status is LoadingFailed<*>
    fun <E> copyWithError(error: E): HoldsData<T>
}

data class Data<T>(
    override val data: T,
    override val status: DataStatus = Initial
) : HoldsData<T> {

    override val copyWithLoadingInProgress: Data<T>
        get() = copy(status = Loading)

    override fun <E> copyWithError(error: E): Data<T> = copy(
        status = LoadingFailed(error)
    )

    fun copyWithNewValue(value: T): Data<T> = copy(
        data = value,
        status = LoadedSuccessfully
    )
}

data class DataList<T>(
    override val data: List<T> = emptyList(),
    override val status: DataStatus = Initial
) : HoldsData<List<T>> {

    override val copyWithLoadingInProgress: DataList<T>
        get() = copy(status = Loading)

    override fun <E> copyWithError(error: E): DataList<T> = copy(
        status = LoadingFailed(error)
    )

    fun transformItems(transform: (T) -> T): DataList<T> = copy(
        data = data.map(transform)
    )

    fun copyWithNewItems(newItems: List<T>): DataList<T> = copy(
        data = data + newItems,
        status = LoadedSuccessfully
    )

    fun copyWithNewItems(vararg newItems: T): DataList<T> = copy(
        data = data + newItems,
        status = LoadedSuccessfully
    )
}

data class PagedDataList<T>(
    override val data: List<T> = emptyList(),
    override val status: DataStatus = Initial,
    val offset: Int = 0,
    val totalItems: Int = Integer.MAX_VALUE
) : HoldsData<List<T>> {

    override val copyWithLoadingInProgress: PagedDataList<T>
        get() = copy(status = Loading)

    override fun <E> copyWithError(error: E): PagedDataList<T> = copy(
        status = LoadingFailed(error)
    )

    fun transformItems(transform: (T) -> T): PagedDataList<T> = copy(
        data = data.map(transform)
    )

    fun copyWithNewItems(
        newItems: List<T>, offset: Int
    ): PagedDataList<T> = copy(
        data = data + newItems,
        offset = offset,
        status = LoadedSuccessfully
    )

    fun copyWithNewItems(
        newItems: List<T>, offset: Int, totalItems: Int
    ): PagedDataList<T> = copy(
        data = data + newItems,
        offset = offset,
        status = LoadedSuccessfully,
        totalItems = totalItems
    )
}

sealed class DataStatus
object Initial : DataStatus()
object Loading : DataStatus()
object LoadedSuccessfully : DataStatus()
data class LoadingFailed<E>(val error: E) : DataStatus()

interface HasFailureMessage {
    val message: String
}