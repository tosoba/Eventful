package com.example.core.repo

import com.example.core.model.app.LocationResult
import kotlinx.coroutines.flow.Flow

interface IAppRepository {
    suspend fun usersLocation(): LocationResult
    val locationAvailable: Flow<Boolean>
}