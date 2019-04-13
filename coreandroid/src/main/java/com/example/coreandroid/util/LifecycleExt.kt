package com.example.coreandroid.util

import androidx.lifecycle.*

operator fun Lifecycle.plusAssign(observer: LifecycleObserver) = addObserver(observer)

fun <T> LiveData<T>.observe(owner: LifecycleOwner, onNext: (T) -> Unit) = observe(owner, Observer(onNext))