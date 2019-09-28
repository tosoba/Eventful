package com.example.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.db.dao.SearchSuggestionDao
import com.example.db.entity.SearchSuggestionEntity

@Database(
    entities = [SearchSuggestionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EventfulDb : RoomDatabase() {
    abstract fun searchSuggestionDao(): SearchSuggestionDao

    companion object {
        const val NAME = "Eventful.db"
    }
}