package com.example.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.db.Tables
import com.example.db.entity.AlarmEntity

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarmEntity: AlarmEntity)

    @Query("DELETE FROM ${Tables.ALARM} WHERE id = :id")
    suspend fun deleteAlarm(id: Long)

    @Query("DELETE FROM ${Tables.ALARM} WHERE id IN (:ids)")
    suspend fun deleteAlarms(ids: List<Long>)
}
