package com.example.repo

import android.annotation.SuppressLint
import android.content.Context
import com.example.core.model.app.FailedToFindLocationException
import com.example.core.model.app.LocationResult
import com.example.core.repo.IAppRepository
import com.example.coreandroid.util.ext.currentLocation
import com.example.coreandroid.util.ext.isLocationAvailable
import com.example.coreandroid.util.ext.latLng
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.patloew.rxlocation.RxLocation
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class AppRepository(
    private val appContext: Context,
    private val rxLocation: RxLocation
) : IAppRepository {

    override suspend fun usersLocation(): LocationResult = try {
        if (appContext.isLocationAvailable) rxLocation.currentLocation()?.let {
            LocationResult.Found(it.latLng)
        } ?: run {
            LocationResult.Error(FailedToFindLocationException)
        } else LocationResult.Disabled
    } catch (e: Exception) {
        LocationResult.Error(e)
    }

    override val locationAvailable: Flow<Boolean>
        get() = Observable.interval(15, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .startWith(0)
            .map { appContext.isLocationAvailable }
            .onErrorReturn { false }
            .distinctUntilChanged()
            .asFlow()

    override val connected: Flow<Boolean>
        @SuppressLint("MissingPermission")
        get() = ReactiveNetwork.observeInternetConnectivity()
            .subscribeOn(Schedulers.computation())
            .distinctUntilChanged()
            .onErrorReturn { false }
            .asFlow()
}