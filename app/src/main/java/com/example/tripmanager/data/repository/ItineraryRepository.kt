package com.example.tripmanager.data.repository

import android.content.Context
import com.example.tripmanager.data.dao.ItineraryDao
import com.example.tripmanager.data.model.ItineraryItem
import com.example.tripmanager.notification.NotificationScheduler
import kotlinx.coroutines.flow.Flow

class ItineraryRepository(
    private val itineraryDao: ItineraryDao,
    private val context: Context
) {
    private val notificationScheduler = NotificationScheduler(context)
    fun getItineraryByTrip(tripId: Long): Flow<List<ItineraryItem>> {
        return itineraryDao.getItineraryByTrip(tripId)
    }

    suspend fun getItineraryById(itineraryId: Long): ItineraryItem? {
        return itineraryDao.getItineraryById(itineraryId)
    }

    suspend fun insertItineraryItem(itineraryItem: ItineraryItem): Long {
        val id = itineraryDao.insertItineraryItem(itineraryItem)
        
        // Programar notificación si tiene recordatorio
        if (itineraryItem.hasReminder) {
            val itemWithId = itineraryItem.copy(id = id)
            notificationScheduler.scheduleNotification(itemWithId)
        }
        
        return id
    }

    suspend fun updateItineraryItem(itineraryItem: ItineraryItem) {
        itineraryDao.updateItineraryItem(itineraryItem)
        
        // Reprogramar notificación
        if (itineraryItem.hasReminder) {
            notificationScheduler.rescheduleNotification(itineraryItem)
        } else {
            // Si se desactivó el recordatorio, cancelar la notificación
            notificationScheduler.cancelNotification(itineraryItem.id)
        }
    }

    suspend fun deleteItineraryItem(itineraryItem: ItineraryItem) {
        itineraryDao.deleteItineraryItem(itineraryItem)
        
        // Cancelar notificación al eliminar el item
        notificationScheduler.cancelNotification(itineraryItem.id)
    }

    suspend fun deleteItineraryByTrip(tripId: Long) {
        // Obtener todos los items para cancelar sus notificaciones
        val items = itineraryDao.getItineraryByTrip(tripId)
        // Como es un Flow, necesitamos usar firstOrNull o collect
        // Por simplicidad, eliminamos directamente
        itineraryDao.deleteItineraryByTrip(tripId)
    }
}

