package com.eventful.core.usecase.location

import com.eventful.core.model.location.LocationResult
import com.eventful.core.repo.IAppRepository
import javax.inject.Inject

class GetLocation @Inject constructor(private val appRepository: IAppRepository) {
    suspend operator fun invoke(): LocationResult = appRepository.usersLocation()
}
