package com.example.coreandroid.util

import androidx.lifecycle.*

operator fun Lifecycle.plusAssign(observer: LifecycleObserver) = addObserver(observer)

operator fun Lifecycle.plusAssign(observers: Collection<LifecycleObserver>) = observers.forEach { addObserver(it) }

fun <T> LiveData<T>.observeUsing(owner: LifecycleOwner, onNext: (T) -> Unit) = observe(owner, Observer(onNext))