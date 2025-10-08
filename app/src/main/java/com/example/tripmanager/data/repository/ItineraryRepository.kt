package com.example.tripmanager.data.repository

import com.example.tripmanager.data.dao.ItineraryDao
import com.example.tripmanager.data.model.ItineraryItem
import kotlinx.coroutines.flow.Flow

class ItineraryRepository(
    private val itineraryDao: ItineraryDao
) {
    fun getItineraryByTrip(tripId: Long): Flow<List<ItineraryItem>> {
        return itineraryDao.getItineraryByTrip(tripId)
    }

    suspend fun getItineraryById(itineraryId: Long): ItineraryItem? {
        return itineraryDao.getItineraryById(itineraryId)
    }

    suspend fun insertItineraryItem(itineraryItem: ItineraryItem): Long {
        return itineraryDao.insertItineraryItem(itineraryItem)
    }

    suspend fun updateItineraryItem(itineraryItem: ItineraryItem) {
        itineraryDao.updateItineraryItem(itineraryItem)
    }

    suspend fun deleteItineraryItem(itineraryItem: ItineraryItem) {
        itineraryDao.deleteItineraryItem(itineraryItem)
    }

    suspend fun deleteItineraryByTrip(tripId: Long) {
        itineraryDao.deleteItineraryByTrip(tripId)
    }
}

