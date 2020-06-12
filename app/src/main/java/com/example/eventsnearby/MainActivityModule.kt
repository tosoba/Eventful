package com.example.eventsnearby

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.provider.ConnectedStateProvider
import com.example.core.provider.LocationStateProvider
import com.example.coreandroid.base.savedStateViewModelFrom
import com.example.coreandroid.di.fragment.AppFragmentFactory
import com.example.coreandroid.di.fragment.FragmentKey
import com.example.coreandroid.di.scope.ActivityScoped
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.di.viewmodel.AssistedSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import com.example.coreandroid.di.viewmodel.ViewModelKey
import com.example.coreandroid.navigation.EventFragmentClassProvider
import com.example.event.EventFragment
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
            EventModule::class,
            WeatherModule::class,
            MainFragmentModule::class
        ]
    )
    abstract fun mainActivity(): MainActivity

    @Binds
    abstract fun fragmentFactory(factory: AppFragmentFactory): FragmentFactory

    @Binds
    @IntoMap
    @FragmentKey(MainFragment::class)
    abstract fun bindMainFragment(fragment: MainFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EventFragment::class)
    abstract fun bindEventFragment(fragment: EventFragment): Fragment

    @Multibinds
    abstract fun fragments(): Map<Class<out ViewModel>, @JvmSuppressWildcards Fragment>

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

    companion object {
        @Provides
        fun mainViewModel(
            mainActivity: MainActivity,
            factory: InjectingSavedStateViewModelFactory
        ): MainViewModel =
            ViewModelProvider(mainActivity, factory.create(mainActivity))[MainViewModel::class.java]

        @Provides
        fun fragmentProvider(): EventFragmentClassProvider = FragmentClassProvider
    }
}
