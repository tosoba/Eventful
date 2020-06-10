package com.example.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.GetNearbyEvents
import com.example.core.usecase.GetPagedEventsFlow
import com.example.core.usecase.SaveEvents
import com.example.coreandroid.di.viewmodel.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.core.provider.ConnectedStateProvider
import com.example.core.provider.LocationStateProvider
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
@Module
abstract class NearbyModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun nearbyFragment(): NearbyFragment

    companion object {
        @Provides
        @IntoMap
        @ViewModelKey(NearbyViewModel::class)
        fun nearbyViewModel(
            getNearbyEvents: GetNearbyEvents,
            saveEvents: SaveEvents,
            getPagedEventsFlow: GetPagedEventsFlow,
            connectedStateProvider: ConnectedStateProvider,
            locationStateProvider: LocationStateProvider,
            ioDispatcher: CoroutineDispatcher
        ): ViewModel = NearbyViewModel(
            getNearbyEvents,
            saveEvents,
            getPagedEventsFlow,
            connectedStateProvider,
            locationStateProvider,
            ioDispatcher
        )
    }
}