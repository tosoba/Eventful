package com.eventful.alarms

import androidx.lifecycle.ViewModel
import com.eventful.core.android.base.savedStateViewModelFrom
import com.eventful.core.android.di.scope.FragmentScoped
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.InjectingSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.ViewModelKey
import com.eventful.core.usecase.alarm.DeleteAlarms
import com.eventful.core.usecase.alarm.GetAlarms
import com.eventful.core.usecase.alarm.CreateAlarm
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
            createAlarm: CreateAlarm,
            ioDispatcher: CoroutineDispatcher
        ): AlarmsFlowProcessor = AlarmsFlowProcessor(
            getAlarms,
            deleteAlarms,
            createAlarm,
            ioDispatcher
        )
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