package com.eventful

import androidx.lifecycle.ViewModel
import com.eventful.alarms.AlarmsModule
import com.eventful.core.android.base.savedStateViewModelFrom
import com.eventful.core.android.di.scope.ActivityScoped
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.InjectingSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.ViewModelKey
import com.eventful.core.android.navigation.IMainChildFragmentNavDestinations
import com.eventful.core.android.provider.ConnectedStateProvider
import com.eventful.core.android.provider.LocationStateProvider
import com.eventful.core.android.provider.PopBackStackSignalProvider
import com.eventful.event.EventModule
import com.eventful.event.IEventChildFragmentsFactory
import com.eventful.event.details.EventDetailsModule
import com.eventful.favourites.FavouritesModule
import com.eventful.nearby.NearbyModule
import com.eventful.search.SearchModule
import com.eventful.weather.WeatherModule
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

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
            AlarmsModule::class,
            EventModule::class,
            EventDetailsModule::class,
            WeatherModule::class,
            MainFragmentModule::class
        ]
    )
    abstract fun mainActivity(): MainActivity

    @Multibinds
    abstract fun viewModels(): Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>

    @Multibinds
    abstract fun assistedViewModelFactories(): Map<Class<out ViewModel>, @JvmSuppressWildcards AssistedSavedStateViewModelFactory<out ViewModel>>

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

    @Binds
    abstract fun popBackStackSignalProvider(mainViewModel: MainViewModel): PopBackStackSignalProvider

    companion object {
        @Provides
        fun mainViewModel(
            mainActivity: MainActivity,
            factory: InjectingSavedStateViewModelFactory
        ): MainViewModel = mainActivity.savedStateViewModelFrom(factory)

        @Provides
        fun mainFragmentNavDestinations(): IMainFragmentNavDestinations = FragmentFactory

        @Provides
        fun mainChildFragmentNavDestinations(): IMainChildFragmentNavDestinations = FragmentFactory

        @Provides
        fun eventChildFragmentsFactory(): IEventChildFragmentsFactory = FragmentFactory
    }
}
