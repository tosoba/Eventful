package com.eventful.nearby

import androidx.lifecycle.ViewModel
import com.eventful.core.android.base.savedStateViewModelFrom
import com.eventful.core.android.di.scope.FragmentScoped
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.InjectingSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.ViewModelKey
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@AssistedModule
@Module(includes = [AssistedInject_NearbyModule::class])
abstract class NearbyModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [NearbyViewModelModule::class])
    abstract fun nearbyFragment(): NearbyFragment

    @Binds
    @IntoMap
    @ViewModelKey(NearbyViewModel::class)
    abstract fun nearbyViewModelFactory(
        factory: NearbyViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    @Module
    object NearbyViewModelModule {
        @Provides
        fun nearbyViewModel(
            nearbyFragment: NearbyFragment,
            factory: InjectingSavedStateViewModelFactory
        ): NearbyViewModel = nearbyFragment.savedStateViewModelFrom(factory)
    }
}
