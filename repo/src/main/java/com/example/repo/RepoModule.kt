package com.example.repo

import com.example.core.repo.IAlarmRepository
import com.example.core.repo.IAppRepository
import com.example.core.repo.IEventRepository
import com.example.core.repo.IWeatherRepository
import dagger.Binds
import dagger.Module
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module
abstract class RepoModule {
    @Binds
    abstract fun alarmRepository(repo: AlarmRepository): IAlarmRepository

    @Binds
    abstract fun appRepository(repo: AppRepository): IAppRepository

    @Binds
    abstract fun eventRepository(repo: EventRepository): IEventRepository

    @Binds
    abstract fun weatherRepository(repo: WeatherRepository): IWeatherRepository
}
