package com.example.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.db.converter.DateConverter
import com.example.db.converter.LinkConverter
import com.example.db.converter.PriceRangeConverter
import com.example.db.converter.StringListConverter
import com.example.db.dao.EventDao
import com.example.db.dao.SearchSuggestionDao
import com.example.db.entity.*

@Database(
    entities = [
        SearchSuggestionEntity::class,
        EventEntity::class,
        EventAttractionJoinEntity::class,
        EventVenueJoinEntity::class,
        AttractionEntity::class,
        VenueEntity::class
    ],
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
    abstract fun eventDao(): EventDao

    companion object {
        const val NAME = "Eventful.db"
    }
}