package com.example.db

import android.content.Context
import androidx.room.Room
import com.example.db.dao.EventDao
import com.example.db.dao.SearchSuggestionDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DbModule {

    @Provides
    @Singleton
    fun eventfulDb(
        context: Context
    ): EventfulDb = Room.databaseBuilder(context, EventfulDb::class.java, EventfulDb.NAME).build()

    @Provides
    @Singleton
    fun searchSuggestionDao(db: EventfulDb): SearchSuggestionDao = db.searchSuggestionDao()

    @Provides
    @Singleton
    fun eventDao(db: EventfulDb): EventDao = db.eventDao()
}
