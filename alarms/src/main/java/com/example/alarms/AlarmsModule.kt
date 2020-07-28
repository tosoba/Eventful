package com.example.alarms

import androidx.lifecycle.ViewModel
import com.example.core.usecase.alarm.DeleteAlarms
import com.example.core.usecase.alarm.GetAlarms
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@AssistedModule
@Module(includes = [AssistedInject_AlarmsModule::class])
abstract class AlarmsModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [AlarmsViewModelModule::class])
    abstract fun alarmsFragment(): AlarmsFragment

    @Binds
    @IntoMap
    @ViewModelKey(AlarmsViewModel::class)
    abstract fun alarmsViewModelFactory(
        factory: AlarmsViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    companion object {
        @Provides
        fun alarmsFlowProcessor(
            getAlarms: GetAlarms,
            deleteAlarms: DeleteAlarms,
            ioDispatcher: CoroutineDispatcher
        ): AlarmsFlowProcessor = AlarmsFlowProcessor(getAlarms, deleteAlarms, ioDispatcher)
    }

    @Module
    object AlarmsViewModelModule {
        @Provides
        fun alarmsViewModel(
            alarmsFragment: AlarmsFragment,
            factory: InjectingSavedStateViewModelFactory
        ): AlarmsViewModel = alarmsFragment.savedStateViewModelFrom(factory)
    }
}