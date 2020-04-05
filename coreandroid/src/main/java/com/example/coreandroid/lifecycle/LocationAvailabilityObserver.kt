package com.example.coreandroid.lifecycle

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.coreandroid.util.ext.isLocationAvailable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LocationAvailabilityObserver(
    private val context: Context,
    private val onChanged: (Boolean) -> Unit,
    private val onError: (Throwable) -> Unit
) : LifecycleObserver {

    constructor(context: Context, onChanged: (Boolean) -> Unit) : this(
        context,
        onChanged,
        { onChanged(false) }
    )

    private var disposable: Disposable? = null

    fun start() {
        stop()
        disposable = Observable.interval(15, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .startWith(0)
            .map { context.isLocationAvailable }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onChanged, onError)
    }

    fun stop() {
        disposable?.dispose()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() = stop()
}