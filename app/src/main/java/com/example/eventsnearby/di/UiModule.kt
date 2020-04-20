package com.example.eventsnearby.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coreandroid.di.ViewModelFactory
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.eventsnearby.FragmentFactory
import com.example.eventsnearby.MainActivityModule
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Provider

@ExperimentalCoroutinesApi
@FlowPreview
@Module(includes = [MainActivityModule::class])
class UiModule {

    @Provides
    fun viewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory = ViewModelFactory(providers)

    @Provides
    fun fragmentProvider(): IFragmentFactory = FragmentFactory
}