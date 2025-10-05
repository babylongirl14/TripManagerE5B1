package com.example.tripmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itinerary_items")
data class ItineraryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long, // Relación con el viaje
    val activityDateTime: Long, // Fecha y hora de la actividad (timestamp)
    val description: String, // Descripción (máx 200 chars)
    val hasReminder: Boolean, // Si tiene recordatorio
    val reminderTimeBefore: String, // "1 hora", "30 min", etc.
    val alertType: AlertType // IMPORTANT o NORMAL
)

enum class AlertType {
    IMPORTANT,
    NORMAL
}

