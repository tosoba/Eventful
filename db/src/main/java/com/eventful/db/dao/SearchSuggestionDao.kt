package com.eventful.db.dao

import androidx.room.*
import com.eventful.db.Tables
import com.eventful.db.entity.SearchSuggestionEntity

@Dao
interface SearchSuggestionDao {
    @Query(
        """SELECT * FROM ${Tables.SEARCH_SUGGESTION} 
        WHERE search_text LIKE :searchText ORDER BY timestamp_ms DESC LIMIT 10"""
    )
    suspend fun getSearchSuggestions(searchText: String): List<SearchSuggestionEntity>

    @Query("SELECT * FROM ${Tables.SEARCH_SUGGESTION} WHERE search_text = :searchText LIMIT 1")
    suspend fun getSuggestion(searchText: String): SearchSuggestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: SearchSuggestionEntity)

    @Query("UPDATE ${Tables.SEARCH_SUGGESTION} SET timestamp_ms = :timestampMs WHERE id = :id")
    suspend fun updateSuggestion(id: Int, timestampMs: Long)

    @Transaction
    suspend fun upsertSuggestion(suggestion: SearchSuggestionEntity) {
        val existing = getSuggestion(suggestion.searchText)
        if (existing == null) insertSuggestion(suggestion)
        else updateSuggestion(existing.id, suggestion.timestampMs)
    }
}
