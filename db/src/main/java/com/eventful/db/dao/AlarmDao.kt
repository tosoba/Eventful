package com.eventful.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eventful.db.Tables
import com.eventful.db.entity.AlarmEntity

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarmEntity: AlarmEntity): Long

    @Query("DELETE FROM ${Tables.ALARM} WHERE id = :id")
    suspend fun deleteAlarm(id: Int)

    @Query("DELETE FROM ${Tables.ALARM} WHERE id IN (:ids)")
    suspend fun deleteAlarms(ids: List<Int>)
}
