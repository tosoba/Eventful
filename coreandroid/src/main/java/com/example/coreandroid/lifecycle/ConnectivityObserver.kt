package com.example.coreandroid.lifecycle

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

@SuppressLint("MissingPermission")
class ConnectivityObserver(
    private val onChanged: (Boolean) -> Unit,
    private val onError: (Throwable) -> Unit
) : LifecycleObserver {

    constructor(onChanged: (Boolean) -> Unit) : this(onChanged, { onChanged(false) })

    private var disposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        disposable = ReactiveNetwork
            .observeInternetConnectivity()
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onChanged, onError)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposable?.dispose()
    }
}