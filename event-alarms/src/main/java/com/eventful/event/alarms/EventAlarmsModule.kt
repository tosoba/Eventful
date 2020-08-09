package com.eventful.event.alarms

import androidx.lifecycle.ViewModel
import com.eventful.alarms.AlarmsFlowProcessor
import com.eventful.core.android.base.savedStateViewModelFrom
import com.eventful.core.android.di.scope.ChildFragmentScoped
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.InjectingSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.ViewModelKey
import com.eventful.core.android.provider.CurrentEventProvider
import com.eventful.core.usecase.alarm.CreateAlarm
import com.eventful.core.usecase.alarm.DeleteAlarms
import com.eventful.core.usecase.alarm.GetAlarms
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
@Module(includes = [AssistedInject_EventAlarmsModule::class])
abstract class EventAlarmsModule {

    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [EventAlarmsViewModelModule::class])
    abstract fun eventAlarmsFragment(): EventAlarmsFragment

    @Binds
    @IntoMap
    @ViewModelKey(EventAlarmsViewModel::class)
    abstract fun eventAlarmsViewModelFactory(
        factory: EventAlarmsViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    companion object {
        @Provides
        @EventAlarmsViewModelProcessor
        fun eventAlarmsFlowProcessor(
            getAlarms: GetAlarms,
            deleteAlarms: DeleteAlarms,
            createAlarm: CreateAlarm,
            currentEventProvider: CurrentEventProvider,
            ioDispatcher: CoroutineDispatcher
        ): AlarmsFlowProcessor = AlarmsFlowProcessor(
            getAlarms,
            deleteAlarms,
            createAlarm,
            currentEventProvider,
            ioDispatcher
        )
    }

    @Module
    object EventAlarmsViewModelModule {
        @Provides
        fun eventAlarmsViewModel(
            eventAlarmsFragment: EventAlarmsFragment,
            factory: InjectingSavedStateViewModelFactory
        ): EventAlarmsViewModel = eventAlarmsFragment.savedStateViewModelFrom(factory)
    }
}
