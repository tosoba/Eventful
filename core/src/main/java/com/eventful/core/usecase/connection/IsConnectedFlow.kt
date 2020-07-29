package com.eventful.core.usecase.connection

import com.eventful.core.repo.IAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsConnectedFlow @Inject constructor(private val appRepository: IAppRepository) {
    operator fun invoke(): Flow<Boolean> = appRepository.connected
}
