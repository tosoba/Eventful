package com.eventful.db

import android.content.Context
import androidx.room.Room
import com.eventful.db.dao.AlarmDao
import com.eventful.db.dao.EventDao
import com.eventful.db.dao.SearchSuggestionDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DbModule {
    @Provides
    @Singleton
    fun eventfulDb(context: Context): EventfulDb =
        Room.databaseBuilder(context, EventfulDb::class.java, EventfulDb.NAME).build()

    @Provides @Singleton fun alarmDao(db: EventfulDb): AlarmDao = db.alarmDao()

    @Provides @Singleton fun eventDao(db: EventfulDb): EventDao = db.eventDao()

    @Provides
    @Singleton
    fun searchSuggestionDao(db: EventfulDb): SearchSuggestionDao = db.searchSuggestionDao()
}
