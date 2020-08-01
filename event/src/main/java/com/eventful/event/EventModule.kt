package com.eventful.event

import com.eventful.core.android.di.scope.FragmentScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
abstract class EventModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun eventFragment(): EventFragment
}