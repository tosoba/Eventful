package com.example.core.usecase

import com.example.core.repo.IAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocationAvailability @Inject constructor(private val appRepository: IAppRepository) {
    operator fun invoke(): Flow<Boolean> = appRepository.locationAvailable
}