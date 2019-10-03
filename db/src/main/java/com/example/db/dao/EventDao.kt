package com.example.db.dao

import androidx.room.*
import com.example.core.model.ticketmaster.IEvent
import com.example.db.Tables
import com.example.db.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

@Dao
interface EventDao {

    @Query("SELECT * FROM ${Tables.EVENT} WHERE id = :id")
    suspend fun getEvent(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("SELECT * FROM ${Tables.EVENT} ORDER BY date_saved DESC LIMIT 20")
    fun getEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM ${Tables.EVENT} WHERE date_saved > :minDate ORDER BY date_saved DESC LIMIT 20")
    suspend fun getEvents(minDate: Date): List<EventEntity>

    @Query("SELECT * FROM ${Tables.EVENT_ATTRACTION_JOIN} WHERE event_id IN (:eventIds)")
    suspend fun getEventsAttractionsIds(eventIds: List<String>): List<EventAttractionJoinEntity>

    @Query("SELECT * FROM ${Tables.EVENT_VENUE_JOIN} WHERE event_id IN (:eventIds)")
    suspend fun getEventsVenuesIds(eventIds: List<String>): List<EventVenueJoinEntity>

    @Query("SELECT * FROM ${Tables.VENUE} WHERE id IN (:ids)")
    fun getVenues(ids: List<String>): List<VenueEntity>

    @Query("SELECT * FROM ${Tables.ATTRACTION} WHERE id IN (:ids)")
    fun getAttractions(ids: List<String>): List<AttractionEntity>

    @Transaction
    suspend fun getFullEvents(
        minDate: Date
    ): List<FullEventEntity> = toFullEvents(getEvents(minDate))

    @Transaction
    fun getFullEventsFlow(): Flow<List<FullEventEntity>> = getEventsFlow().map { toFullEvents(it) }

    @Query("DELETE FROM ${Tables.EVENT} WHERE id = :id")
    suspend fun deleteEvent(id: String)

    companion object {
        suspend fun EventDao.toFullEvents(eventEntities: List<EventEntity>): List<FullEventEntity> {
            val eventIds = eventEntities.map { it.id }
            val eventVenueJoins = getEventsVenuesIds(eventIds)
            val groupedVenueIds = eventVenueJoins.groupBy({ it.eventId }, { it.venueId })
            val eventAttractionJoins = getEventsAttractionsIds(eventIds)
            val groupedAttractionIds = eventAttractionJoins
                .groupBy({ it.eventId }, { it.attractionId })
            val venues = getVenues(eventVenueJoins.map { it.venueId })
                .map { it.id to it }
                .toMap()
            val attractions = getAttractions(eventAttractionJoins.map { it.attractionId })
                .map { it.id to it }
                .toMap()
            return eventEntities.map { eventEntity ->
                FullEventEntity(
                    event = eventEntity,
                    attractions = groupedAttractionIds[eventEntity.id]?.mapNotNull { attractions[it] }
                        ?: emptyList(),
                    venues = groupedVenueIds[eventEntity.id]?.mapNotNull { venues[it] }
                        ?: emptyList()
                )
            }
        }
    }
}