package com.example.db.converter

import androidx.room.TypeConverter

class StringListConverter {

    @TypeConverter
    fun toStringList(listStr: String?): List<String>? = listStr?.split(",")

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.joinToString(separator = ",")
}
