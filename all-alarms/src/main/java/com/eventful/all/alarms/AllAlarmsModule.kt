package com.eventful.all.alarms

import androidx.lifecycle.ViewModel
import com.eventful.alarms.AlarmsFlowProcessor
import com.eventful.core.android.base.savedStateViewModelFrom
import com.eventful.core.android.di.scope.ChildFragmentScoped
import com.eventful.core.android.di.viewmodel.AssistedSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.InjectingSavedStateViewModelFactory
import com.eventful.core.android.di.viewmodel.ViewModelKey
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
@Module(includes = [AssistedInject_AllAlarmsModule::class])
abstract class AllAlarmsModule {

    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [AllAlarmsViewModelModule::class])
    abstract fun allAlarmsFragment(): AllAlarmsFragment

    @Binds
    @IntoMap
    @ViewModelKey(AllAlarmsViewModel::class)
    abstract fun allAlarmsViewModelFactory(
        factory: AllAlarmsViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    companion object {
        @Provides
        @AllAlarmsViewModelProcessor
        fun allAlarmsFlowProcessor(
            getAlarms: GetAlarms,
            deleteAlarms: DeleteAlarms,
            createAlarm: CreateAlarm,
            ioDispatcher: CoroutineDispatcher
        ): AlarmsFlowProcessor = AlarmsFlowProcessor(
            getAlarms,
            deleteAlarms,
            createAlarm,
            null,
            ioDispatcher
        )
    }

    @Module
    object AllAlarmsViewModelModule {
        @Provides
        fun allAlarmsViewModel(
            allAlarmsFragment: AllAlarmsFragment,
            factory: InjectingSavedStateViewModelFactory
        ): AllAlarmsViewModel = allAlarmsFragment.savedStateViewModelFrom(factory)
    }
}
