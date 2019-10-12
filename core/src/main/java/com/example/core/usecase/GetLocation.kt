package com.example.core.usecase

import com.example.core.model.app.LocationState
import com.example.core.repo.IAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocation @Inject constructor(private val appRepository: IAppRepository) {
    operator fun invoke(): Flow<LocationState> = appRepository.usersLocation
}