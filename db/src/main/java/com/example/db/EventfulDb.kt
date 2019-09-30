package com.example.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.db.converter.DateConverter
import com.example.db.converter.LinkConverter
import com.example.db.converter.PriceRangeConverter
import com.example.db.converter.StringListConverter
import com.example.db.dao.SearchSuggestionDao
import com.example.db.entity.SearchSuggestionEntity

@Database(
    entities = [SearchSuggestionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    value = [
        DateConverter::class,
        StringListConverter::class,
        PriceRangeConverter::class,
        LinkConverter::class
    ]
)
abstract class EventfulDb : RoomDatabase() {
    abstract fun searchSuggestionDao(): SearchSuggestionDao

    companion object {
        const val NAME = "Eventful.db"
    }
}