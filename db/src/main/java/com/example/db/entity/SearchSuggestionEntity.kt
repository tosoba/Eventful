package com.example.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.db.Tables

@Entity(tableName = Tables.SEARCH_SUGGESTION)
data class SearchSuggestionEntity(
    @ColumnInfo(name = "search_text") val searchText: String,
    @ColumnInfo(name = "timestamp_ms") val timestampMs: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
