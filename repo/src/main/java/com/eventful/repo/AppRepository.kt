package com.eventful.repo

import android.annotation.SuppressLint
import android.content.Context
import com.eventful.core.model.location.FailedToFindLocationException
import com.eventful.core.model.location.LocationResult
import com.eventful.core.repo.IAppRepository
import com.eventful.core.android.util.ext.currentLocation
import com.eventful.core.android.util.ext.isLocationAvailable
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.patloew.rxlocation.RxLocation
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class AppRepository @Inject constructor(
    private val appContext: Context,
    private val rxLocation: RxLocation
) : IAppRepository {

    override suspend fun usersLocation(): LocationResult = try {
        if (appContext.isLocationAvailable) rxLocation.currentLocation()?.let {
            LocationResult.Found(it.latitude, it.longitude)
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
        get() = ReactiveNetwork.observeNetworkConnectivity(appContext)
            .map { it.available() }
            .subscribeOn(Schedulers.computation())
            .distinctUntilChanged()
            .onErrorReturn { false }
            .asFlow()
}
