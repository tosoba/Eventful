package com.example.eventsnearby.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coreandroid.di.ViewModelFactory
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.eventsnearby.FragmentProvider
import com.example.eventsnearby.MainActivityModule
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module(
    includes = [
        MainActivityModule::class
    ]
)
class UiModule {

    @Provides
    fun viewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory = ViewModelFactory(providers)

    @Provides
    fun fragmentProvider(): IFragmentProvider = FragmentProvider
}