package com.example.tripmanager.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tripmanager.data.model.AlertType

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val itineraryId = intent.getLongExtra(NotificationHelper.EXTRA_ITINERARY_ID, -1)
        val tripId = intent.getLongExtra(NotificationHelper.EXTRA_TRIP_ID, -1)
        val description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION) ?: ""
        val alertTypeString = intent.getStringExtra(NotificationHelper.EXTRA_ALERT_TYPE) ?: "NORMAL"
        val activityTime = intent.getLongExtra("activity_time", System.currentTimeMillis())
        
        if (itineraryId == -1L || description.isEmpty()) {
            return
        }

        val alertType = try {
            AlertType.valueOf(alertTypeString)
        } catch (e: Exception) {
            AlertType.NORMAL
        }

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(
            itineraryId = itineraryId,
            tripId = tripId,
            description = description,
            alertType = alertType,
            activityTime = activityTime
        )
    }
}

