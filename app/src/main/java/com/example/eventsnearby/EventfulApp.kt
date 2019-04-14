package com.example.eventsnearby

import android.app.Application
import com.example.eventsnearby.di.eventsDataModule
import com.example.eventsnearby.di.mainModule
import com.example.nearby.nearbyModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EventfulApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EventfulApp)
            modules(mainModule, eventsDataModule, nearbyModule)
        }
    }
}