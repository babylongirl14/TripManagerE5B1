package com.example.tripmanager.data.dao

import androidx.room.*
import com.example.tripmanager.data.model.ItineraryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ItineraryDao {
    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId ORDER BY activityDateTime ASC")
    fun getItineraryByTrip(tripId: Long): Flow<List<ItineraryItem>>

    @Query("SELECT * FROM itinerary_items WHERE id = :itineraryId")
    suspend fun getItineraryById(itineraryId: Long): ItineraryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItineraryItem(itineraryItem: ItineraryItem): Long

    @Update
    suspend fun updateItineraryItem(itineraryItem: ItineraryItem)

    @Delete
    suspend fun deleteItineraryItem(itineraryItem: ItineraryItem)

    @Query("DELETE FROM itinerary_items WHERE tripId = :tripId")
    suspend fun deleteItineraryByTrip(tripId: Long)
}

