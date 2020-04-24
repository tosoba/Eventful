package com.example.repo

import android.content.Context
import com.example.core.model.app.FailedToFindLocationException
import com.example.core.model.app.LocationResult
import com.example.core.repo.IAppRepository
import com.example.coreandroid.util.ext.currentLocation
import com.example.coreandroid.util.ext.isLocationAvailable
import com.example.coreandroid.util.ext.latLng
import com.example.coreandroid.util.ext.locationEnabled
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.patloew.rxlocation.RxLocation
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.rx2.openSubscription
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class AppRepository(
    private val appContext: Context,
    private val rxLocation: RxLocation
) : IAppRepository {

    //TODO: use either locationEnabled or isLocationAvailable -> decide which one is better
    override suspend fun usersLocation(): LocationResult = try {
        if (appContext.locationEnabled) rxLocation.currentLocation()?.let {
            LocationResult.Found(it.latLng)
        } ?: run {
            LocationResult.Error(FailedToFindLocationException())
        } else LocationResult.Disabled
    } catch (e: Exception) {
        LocationResult.Error(e)
    }

    override val locationAvailable: Flow<Boolean>
        get() = Observable.interval(15, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .startWith(0)
            .map { appContext.isLocationAvailable }
            .distinctUntilChanged()
            .onErrorReturn { false }
            .openSubscription()
            .consumeAsFlow()

    override val connected: Flow<Boolean>
        get() = ReactiveNetwork
            .observeInternetConnectivity()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .onErrorReturn { false }
            .openSubscription()
            .consumeAsFlow()
}