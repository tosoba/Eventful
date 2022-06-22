package com.eventful.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eventful.db.converter.DateConverter
import com.eventful.db.converter.LinkConverter
import com.eventful.db.converter.PriceRangeConverter
import com.eventful.db.converter.StringListConverter
import com.eventful.db.dao.AlarmDao
import com.eventful.db.dao.EventDao
import com.eventful.db.dao.SearchSuggestionDao
import com.eventful.db.entity.*

@Database(
    entities =
        [
            SearchSuggestionEntity::class,
            EventEntity::class,
            EventAttractionJoinEntity::class,
            EventVenueJoinEntity::class,
            AttractionEntity::class,
            VenueEntity::class,
            AlarmEntity::class],
    version = 1,
    exportSchema = false)
@TypeConverters(
    value =
        [
            DateConverter::class,
            StringListConverter::class,
            PriceRangeConverter::class,
            LinkConverter::class])
abstract class EventfulDb : RoomDatabase() {
    abstract fun searchSuggestionDao(): SearchSuggestionDao
    abstract fun eventDao(): EventDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val NAME = "Eventful.db"
    }
}
