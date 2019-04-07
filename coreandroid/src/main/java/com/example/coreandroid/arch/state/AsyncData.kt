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

