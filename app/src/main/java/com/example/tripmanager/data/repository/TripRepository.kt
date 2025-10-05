package com.example.tripmanager.data.repository

import com.example.tripmanager.data.dao.TripDao
import com.example.tripmanager.data.model.Trip
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao
) {
    fun getTripsByUser(username: String): Flow<List<Trip>> {
        return tripDao.getTripsByUser(username)
    }

    suspend fun getTripById(tripId: Long): Trip? {
        return tripDao.getTripById(tripId)
    }

    suspend fun insertTrip(trip: Trip): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }
}
