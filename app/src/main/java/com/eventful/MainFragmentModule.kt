package com.eventful

import com.eventful.core.android.di.scope.FragmentScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
abstract class MainFragmentModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun mainNavigationFragment(): MainNavigationFragment

    @FragmentScoped @ContributesAndroidInjector abstract fun mainFragment(): MainFragment
}
