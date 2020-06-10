package com.example.eventsnearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.provider.ConnectedStateProvider
import com.example.core.provider.LocationStateProvider
import com.example.coreandroid.di.scope.ActivityScoped
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.ViewModelKey
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.event.EventModule
import com.example.favourites.FavouritesModule
import com.example.nearby.NearbyModule
import com.example.search.SearchModule
import com.example.weather.WeatherModule
import com.squareup.inject.assisted.dagger2.AssistedModule
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
@AssistedModule
@Module(includes = [AssistedInject_MainActivityModule::class])
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
    abstract fun mainViewModelFactory(
        factory: MainViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    @Binds
    abstract fun connectivityStateProvider(mainViewModel: MainViewModel): ConnectedStateProvider

    @Binds
    abstract fun locationStateProvider(mainViewModel: MainViewModel): LocationStateProvider

    companion object {

        @Provides
        fun mainViewModel(
            mainActivity: MainActivity,
            factory: InjectingSavedStateViewModelFactory
        ): MainViewModel = ViewModelProvider(
            mainActivity,
            factory.create(mainActivity)
        )[MainViewModel::class.java]

        @Provides
        fun viewModelFactory(
            assistedFactories: Map<Class<out ViewModel>, @JvmSuppressWildcards AssistedSavedStateViewModelFactory<out ViewModel>>,
            viewModelProviders: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
        ): InjectingSavedStateViewModelFactory = InjectingSavedStateViewModelFactory(
            assistedFactories,
            viewModelProviders
        )

        @Provides
        fun fragmentProvider(): IFragmentFactory = FragmentFactory
    }
}