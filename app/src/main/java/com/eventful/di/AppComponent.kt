package com.eventful.di

import com.eventful.EventfulApp
import com.eventful.MainActivityModule
import com.eventful.darsky.api.DarkSkyApiModule
import com.eventful.db.DbModule
import com.eventful.repo.RepoModule
import com.eventful.ticketmaster.api.TicketMasterApiModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules =
        [
            ThreadingModule::class,
            AppModule::class,
            TicketMasterApiModule::class,
            DarkSkyApiModule::class,
            DbModule::class,
            RepoModule::class,
            AndroidSupportInjectionModule::class,
            MainActivityModule::class])
interface AppComponent : AndroidInjector<EventfulApp> {
    @Component.Factory abstract class Factory : AndroidInjector.Factory<EventfulApp>
}
