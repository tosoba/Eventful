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

    @Query("SELECT * FROM ${Tables.EVENT} WHERE id = :id")
    fun getEventFlow(id: String): Flow<EventEntity?>

    @Query("SELECT * FROM ${Tables.EVENT} WHERE id IN (:ids)")
    suspend fun getEvents(ids: List<String>): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenues(venues: List<VenueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttractions(attractions: List<AttractionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun joinEventsAttractions(entities: List<EventAttractionJoinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun joinEventsVenues(entities: List<EventVenueJoinEntity>)

    @Transaction
    suspend fun insertFullEvents(events: List<IEvent>) {
        val existingIds = getEvents(events.map { it.id }).map { it.id }.toSet()
        val eventsToInsert = events.filter { !existingIds.contains(it.id) }
        if (eventsToInsert.isEmpty()) return

        val venues: ArrayList<VenueEntity> = ArrayList()
        val eventVenueJoinEntities: ArrayList<EventVenueJoinEntity> = ArrayList()
        val attractions: ArrayList<AttractionEntity> = ArrayList()
        val eventAttractionJoinEntities: ArrayList<EventAttractionJoinEntity> = ArrayList()
        eventsToInsert.forEach { event ->
            event.venues?.let { eventVenues ->
                venues.addAll(eventVenues.map { VenueEntity(it) })
                eventVenueJoinEntities.addAll(eventVenues.map {
                    EventVenueJoinEntity(event.id, it.id)
                })
            }

            event.attractions?.let { eventAttractions ->
                attractions.addAll(eventAttractions.map { AttractionEntity(it) })
                eventAttractionJoinEntities.addAll(eventAttractions.map {
                    EventAttractionJoinEntity(event.id, it.id)
                })
            }
        }

        insertEvents(eventsToInsert.map { EventEntity(it) })
        if (venues.isNotEmpty()) {
            insertVenues(venues)
            joinEventsVenues(eventVenueJoinEntities)
        }
        if (attractions.isNotEmpty()) {
            insertAttractions(attractions)
            joinEventsAttractions(eventAttractionJoinEntities)
        }
    }

    @Transaction
    suspend fun insertEvent(event: IEvent): Boolean = if (getEvent(event.id) == null) {
        insertEvent(EventEntity(event))
        event.venues?.let { venues ->
            insertVenues(venues.map { VenueEntity(it) })
            joinEventsVenues(venues.map { EventVenueJoinEntity(event.id, it.id) })
        }
        event.attractions?.let { attractions ->
            insertAttractions(attractions.map { AttractionEntity(it) })
            joinEventsAttractions(attractions.map {
                EventAttractionJoinEntity(event.id, it.id)
            })
        }
        true
    } else false

    @Transaction
    @Query("SELECT * FROM ${Tables.EVENT} ORDER BY date_saved DESC LIMIT :limit")
    fun getEventsFlow(limit: Int): Flow<List<FullEventEntity>>

    @Query("SELECT * FROM ${Tables.EVENT_ATTRACTION_JOIN} WHERE event_id IN (:eventIds)")
    suspend fun getEventsAttractionsIds(eventIds: List<String>): List<EventAttractionJoinEntity>

    @Query("SELECT * FROM ${Tables.EVENT_VENUE_JOIN} WHERE event_id IN (:eventIds)")
    suspend fun getEventsVenuesIds(eventIds: List<String>): List<EventVenueJoinEntity>

    @Query("SELECT * FROM ${Tables.VENUE} WHERE id IN (:ids)")
    fun getVenues(ids: List<String>): List<VenueEntity>

    @Query("SELECT * FROM ${Tables.ATTRACTION} WHERE id IN (:ids)")
    fun getAttractions(ids: List<String>): List<AttractionEntity>

    @Query("DELETE FROM ${Tables.EVENT} WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @Query("DELETE FROM ${Tables.EVENT} WHERE id IN (:ids)")
    suspend fun deleteEvents(ids: List<String>)
}