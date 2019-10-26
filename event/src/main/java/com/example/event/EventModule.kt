package com.example.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.usecase.DeleteEvent
import com.example.core.usecase.IsEventSaved
import com.example.core.usecase.SaveEvent
import com.example.coreandroid.di.ViewModelKey
import com.example.coreandroid.di.scope.FragmentScoped
import com.example.coreandroid.util.Data
import com.example.coreandroid.util.Initial
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

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
            isEventSaved: IsEventSaved,
            saveEvent: SaveEvent,
            deleteEvent: DeleteEvent
        ): ViewModel = EventViewModel(initialState, isEventSaved, saveEvent, deleteEvent)

        @Provides
        fun eventInitialState(
            fragment: EventFragment
        ): EventState = EventState(fragment.event, Data(false, Initial))

        @Provides
        fun eventViewModel(
            factory: ViewModelProvider.Factory, target: EventFragment
        ): EventViewModel = ViewModelProvider(target, factory).get(EventViewModel::class.java)
    }
}