package com.example.db.converter

import androidx.room.TypeConverter
import java.util.*

object DateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): Date? = if (dateLong == null) null else Date(dateLong)

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time
}