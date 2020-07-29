package com.eventful.db.converter

import androidx.room.TypeConverter
import com.eventful.db.entity.LinkEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class LinkConverter {
    @TypeConverter
    fun stringToLinkList(data: String?): List<LinkEntity> = if (data == null) {
        Collections.emptyList()
    } else {
        Gson().fromJson(data, object : TypeToken<List<LinkEntity>>() {}.type)
    }

    @TypeConverter
    fun linkListToString(
        links: List<LinkEntity>?
    ): String? = if (links == null || links.isEmpty()) null else Gson().toJson(links)
}
