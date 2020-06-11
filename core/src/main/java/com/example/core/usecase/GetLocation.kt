package com.example.core.usecase

import com.example.core.model.app.LocationResult
import com.example.core.repo.IAppRepository
import javax.inject.Inject

class GetLocation @Inject constructor(private val appRepository: IAppRepository) {
    suspend operator fun invoke(): LocationResult = appRepository.usersLocation()
}
