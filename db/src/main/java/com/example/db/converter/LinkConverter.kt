package com.example.db.converter

import androidx.room.TypeConverter
import com.example.db.entity.LinkEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class LinkConverter {

    @TypeConverter
    fun storedStringToMyObjects(
        data: String?
    ): List<LinkEntity> = if (data == null) Collections.emptyList()
    else Gson().fromJson(data, object : TypeToken<List<LinkEntity>>() {}.type)

    @TypeConverter
    fun myObjectsToStoredString(myObjects: List<LinkEntity>): String = Gson().toJson(myObjects)
}