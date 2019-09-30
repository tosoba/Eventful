package com.example.db.dao

import androidx.room.*
import com.example.core.model.ticketmaster.IEvent
import com.example.db.Tables
import com.example.db.entity.*

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenues(venues: List<VenueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttractions(attractions: List<AttractionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun joinEventsAttractions(entities: List<EventAttractionJoinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun joinEventsVenues(entities: List<EventVenueJoinEntity>)

    @Transaction
    suspend fun insertEvent(event: IEvent) {
        insertEvent(EventEntity(event))
        //TODO: see if transaction is properly aborted here (and if there's a way to check that it happened)
        insertVenues(event.venues.map { VenueEntity(it) })
        joinEventsVenues(event.venues.map { EventVenueJoinEntity(event.id, it.id) })
        insertAttractions(event.attractions.map { AttractionEntity(it) })
        joinEventsAttractions(event.attractions.map { EventAttractionJoinEntity(event.id, it.id) })
    }

    @Query("DELETE FROM ${Tables.EVENT} WHERE id = :id")
    suspend fun deleteEvent(id: String)
}