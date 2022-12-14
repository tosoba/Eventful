package com.eventful.core.usecase.location

import com.eventful.core.repo.IAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsLocationAvailableFlow @Inject constructor(private val appRepository: IAppRepository) {
    operator fun invoke(): Flow<Boolean> = appRepository.locationAvailable
}
