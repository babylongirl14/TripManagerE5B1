package com.example.tripmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val destination: String,
    val startDate: Long, // Stored as timestamp
    val endDate: Long,   // Stored as timestamp
    val tripType: TripType,
    val username: String // Foreign key to User
)

enum class TripType {
    VACATION,
    WORK
}
