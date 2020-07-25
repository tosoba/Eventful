package com.example.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.db.Tables
import com.example.db.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM ${Tables.ALARM} WHERE event_id = :eventId")
    fun getAlarmsForEvent(eventId: String): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM ${Tables.ALARM}")
    fun getAlarms(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarmEntity: AlarmEntity)

    @Query("DELETE FROM ${Tables.ALARM} WHERE id = :id")
    suspend fun deleteAlarm(id: Long)

    @Query("DELETE FROM ${Tables.ALARM} WHERE id IN (:ids)")
    suspend fun deleteAlarms(ids: List<Long>)
}
