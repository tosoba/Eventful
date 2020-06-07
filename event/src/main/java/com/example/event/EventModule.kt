package com.example.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSavedFlow
import com.example.core.usecase.SaveEvent
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.core.util.Data
import com.example.core.util.Initial
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
abstract class EventModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [ModuleProvides::class])
    abstract fun eventFragment(): EventFragment

    @Module
    class ModuleProvides {

        @Provides
        @IntoMap
        @ViewModelKey(EventViewModel::class)
        fun eventViewModelBase(
            initialState: EventState,
            isEventSavedFlow: IsEventSavedFlow,
            saveEvent: SaveEvent,
            deleteEvent: DeleteEvent
        ): ViewModel = EventViewModel(initialState, isEventSavedFlow, saveEvent, deleteEvent)

        @Provides
        fun eventInitialState(
            fragment: EventFragment
        ): EventState = EventState(fragment.event,
            Data(
                false,
                Initial
            )
        )

        @Provides
        fun eventViewModel(
            factory: ViewModelProvider.Factory, target: EventFragment
        ): EventViewModel = ViewModelProvider(target, factory).get(EventViewModel::class.java)
    }
}