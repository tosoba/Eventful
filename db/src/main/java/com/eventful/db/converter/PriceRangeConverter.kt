package com.eventful.db.converter

import androidx.room.TypeConverter
import com.eventful.db.entity.PriceRangeEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Collections.emptyList

class PriceRangeConverter {
    @TypeConverter
    fun stringToPriceRangeList(data: String?): List<PriceRangeEntity> = if (data == null) {
        emptyList()
    } else {
        Gson().fromJson(data, object : TypeToken<List<PriceRangeEntity>>() {}.type)
    }

    @TypeConverter
    fun priceRangeListToString(
        priceRanges: List<PriceRangeEntity>?
    ): String? = if (priceRanges == null || priceRanges.isEmpty()) {
        null
    } else {
        Gson().toJson(priceRanges)
    }
}
