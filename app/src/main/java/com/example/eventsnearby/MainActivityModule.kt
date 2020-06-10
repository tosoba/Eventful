package com.example.eventsnearby

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.provider.ConnectedStateProvider
import com.example.core.provider.LocationStateProvider
import com.example.core.usecase.GetLocation
import com.example.core.usecase.IsConnectedFlow
import com.example.core.usecase.IsLocationAvailableFlow
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.ViewModelKey
import com.example.coreandroid.di.scope.ActivityScoped
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.event.EventModule
import com.example.favourites.FavouritesModule
import com.example.nearby.NearbyModule
import com.example.search.SearchModule
import com.example.weather.WeatherModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Provider

@ExperimentalCoroutinesApi
@FlowPreview
@Module
abstract class MainActivityModule {

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            NearbyModule::class,
            SearchModule::class,
            FavouritesModule::class,
            EventModule::class,
            WeatherModule::class,
            MainFragmentModule::class
        ]
    )
    abstract fun mainActivity(): MainActivity

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun mainViewModelBase(viewModel: MainViewModel): ViewModel

    @Binds
    abstract fun connectivityStateProvider(mainViewModel: MainViewModel): ConnectedStateProvider

    @Binds
    abstract fun locationStateProvider(mainViewModel: MainViewModel): LocationStateProvider

    companion object {

        @Provides
        fun mainViewModel(
            getLocation: GetLocation,
            isConnectedFlow: IsConnectedFlow,
            isLocationAvailableFlow: IsLocationAvailableFlow,
            appContext: Context
        ): MainViewModel = MainViewModel(
            getLocation,
            isConnectedFlow,
            isLocationAvailableFlow,
            appContext
        )

        @Provides
        fun viewModelFactory(
            viewModelProviders: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
        ): InjectingSavedStateViewModelFactory = InjectingSavedStateViewModelFactory(
            emptyMap(),
            viewModelProviders
        )

        @Provides
        fun fragmentProvider(): IFragmentFactory = FragmentFactory
    }
}