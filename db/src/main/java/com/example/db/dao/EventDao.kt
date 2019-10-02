package com.example.db.dao

import androidx.room.*
import com.example.core.model.ticketmaster.IEvent
import com.example.db.Tables
import com.example.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM ${Tables.EVENT} WHERE id = :id")
    suspend fun getEvent(id: String): EventEntity?

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
    suspend fun insertEvent(event: IEvent): Boolean = if (getEvent(event.id) == null) {
        insertEvent(EventEntity(event))
        insertVenues(event.venues.map { VenueEntity(it) })
        joinEventsVenues(event.venues.map { EventVenueJoinEntity(event.id, it.id) })
        insertAttractions(event.attractions.map { AttractionEntity(it) })
        joinEventsAttractions(event.attractions.map {
            EventAttractionJoinEntity(event.id, it.id)
        })
        true
    } else false

    @Query("SELECT * FROM ${Tables.EVENT}")
    suspend fun getEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM ${Tables.EVENT_ATTRACTION_JOIN} WHERE event_id IN (:eventIds)")
    suspend fun getEventsAttractionsIds(eventIds: List<String>): List<EventAttractionJoinEntity>

    @Query("SELECT * FROM ${Tables.EVENT_VENUE_JOIN} WHERE event_id IN (:eventIds)")
    suspend fun getEventsVenuesIds(eventIds: List<String>): List<EventVenueJoinEntity>

    @Query("SELECT * FROM ${Tables.VENUE} WHERE id IN (:ids)")
    fun getVenues(ids: List<String>): List<VenueEntity>

    @Query("SELECT * FROM ${Tables.ATTRACTION} WHERE id IN (:ids)")
    fun getAttractions(ids: List<String>): List<AttractionEntity>

//    @Transaction
//    suspend fun getEventsFlow(): Flow<List<FullEventEntity>> {
//        getEvents()
//            .map { eventEntities ->
//                val eventIds = eventEntities.map { it.id }
//                val eventVenueJoins = getEventsVenuesIds(eventIds)
//                val eventAttractionJoins = getEventsAttractionsIds(eventIds)
//
//            }
//    }

    @Query("DELETE FROM ${Tables.EVENT} WHERE id = :id")
    suspend fun deleteEvent(id: String)
}