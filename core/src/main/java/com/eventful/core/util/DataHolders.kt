package com.eventful.core.util

interface HoldsData<T> {
    val data: T
    val status: DataStatus
    val loadingFailed: Boolean
        get() = status is Failure
    val copyWithLoadingStatus: HoldsData<T>
    fun copyWithFailureStatus(error: Any?): HoldsData<T>
}

data class Data<T>(override val data: T, override val status: DataStatus = Initial) : HoldsData<T> {
    override val copyWithLoadingStatus: Data<T>
        get() = copy(status = Loading)
    override fun copyWithFailureStatus(error: Any?): Data<T> = copy(status = Failure(error))
    fun copyWithNewValue(value: T): Data<T> = copy(data = value, status = LoadedSuccessfully)
}

interface HoldsList<T> : HoldsData<List<T>> {
    val canLoadMore: Boolean
}

data class DataList<T>(
    override val data: List<T> = emptyList(),
    override val status: DataStatus = Initial,
    val limitHit: Boolean = false
) : HoldsList<T> {

    override val canLoadMore: Boolean
        get() = !limitHit
    override val copyWithLoadingStatus: DataList<T>
        get() = copy(status = Loading)
    override fun copyWithFailureStatus(error: Any?): DataList<T> = copy(status = Failure(error))

    fun transformItems(transform: (T) -> T): DataList<T> = copy(data = data.map(transform))

    fun copyWithNewItems(newItems: List<T>): DataList<T> =
        copy(data = data + newItems, status = LoadedSuccessfully)

    fun copyWithNewItems(vararg newItems: T): DataList<T> =
        copy(data = data + newItems, status = LoadedSuccessfully)
}

data class PagedDataList<T>(
    override val data: List<T> = emptyList(),
    override val status: DataStatus = Initial,
    val offset: Int = 0,
    val limit: Int = Integer.MAX_VALUE
) : HoldsList<T> {

    override val canLoadMore: Boolean
        get() = offset < limit
    override val copyWithLoadingStatus: PagedDataList<T>
        get() = copy(status = Loading)
    override fun copyWithFailureStatus(error: Any?): PagedDataList<T> =
        copy(status = Failure(error))

    fun transformItems(transform: (T) -> T): PagedDataList<T> = copy(data = data.map(transform))

    fun copyWithNewItems(newItems: List<T>, offset: Int, limit: Int): PagedDataList<T> =
        copy(data = data + newItems, offset = offset, status = LoadedSuccessfully, limit = limit)
}

sealed class DataStatus

object Initial : DataStatus()

object Loading : DataStatus()

object LoadedSuccessfully : DataStatus()

data class Failure(val error: Any?) : DataStatus()

interface HasFailureMessage {
    val message: String
}
