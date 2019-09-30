package com.example.db.converter

import androidx.room.TypeConverter
import com.example.db.entity.PriceRangeEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Collections.emptyList

class PriceRangeConverter {

    @TypeConverter
    fun storedStringToMyObjects(
        data: String?
    ): List<PriceRangeEntity> = if (data == null) emptyList()
    else Gson().fromJson(data, object : TypeToken<List<PriceRangeEntity>>() {}.type)

    @TypeConverter
    fun myObjectsToStoredString(
        myObjects: List<PriceRangeEntity>
    ): String = Gson().toJson(myObjects)
}