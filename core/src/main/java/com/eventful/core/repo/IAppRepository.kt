package com.eventful.core.repo

import com.eventful.core.model.location.LocationResult
import kotlinx.coroutines.flow.Flow

interface IAppRepository {
    suspend fun usersLocation(): LocationResult
    val locationAvailable: Flow<Boolean>
    val connected: Flow<Boolean>
}
