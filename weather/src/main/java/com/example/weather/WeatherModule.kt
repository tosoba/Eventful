package com.example.weather

import androidx.lifecycle.ViewModel
import com.example.coreandroid.base.savedStateViewModelFrom
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.ViewModelKey
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@AssistedModule
@Module(includes = [AssistedInject_WeatherModule::class])
abstract class WeatherModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [WeatherViewModelModule::class])
    abstract fun weatherFragment(): WeatherFragment

    @Binds
    @IntoMap
    @ViewModelKey(WeatherViewModel::class)
    abstract fun weatherViewModelFactory(
        factory: WeatherViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    @Module
    object WeatherViewModelModule {
        @Provides
        fun weatherViewModel(
            weatherFragment: WeatherFragment,
            factory: InjectingSavedStateViewModelFactory
        ): WeatherViewModel = weatherFragment.savedStateViewModelFrom(factory)
    }
}
