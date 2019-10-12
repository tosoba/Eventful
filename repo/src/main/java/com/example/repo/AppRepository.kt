package com.example.repo

import android.content.Context
import com.example.core.model.app.FailedToFindLocationException
import com.example.core.model.app.LocationState
import com.example.core.repo.IAppRepository
import com.example.coreandroid.util.ext.currentLocation
import com.example.coreandroid.util.ext.latLng
import com.example.coreandroid.util.ext.locationEnabled
import com.patloew.rxlocation.RxLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AppRepository(
    private val appContext: Context,
    private val rxLocation: RxLocation
) : IAppRepository {

    override val usersLocation: Flow<LocationState>
        get() = flow {
            emit(LocationState.Loading)
            try {
                if (appContext.locationEnabled) rxLocation.currentLocation()?.let {
                    emit(LocationState.Found(it.latLng))
                } ?: run {
                    emit(LocationState.Error(FailedToFindLocationException()))
                } else emit(LocationState.Disabled)
            } catch (e: Exception) {
                emit(LocationState.Error(e))
            }
        }
}