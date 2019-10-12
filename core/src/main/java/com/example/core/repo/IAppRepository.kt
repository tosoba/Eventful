package com.example.core.repo

import com.example.core.model.app.LocationState
import kotlinx.coroutines.flow.Flow

interface IAppRepository {
    val usersLocation: Flow<LocationState>
}