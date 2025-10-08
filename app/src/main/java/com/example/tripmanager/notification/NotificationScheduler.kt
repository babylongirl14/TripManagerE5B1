package com.example.tripmanager.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.tripmanager.data.model.AlertType
import com.example.tripmanager.data.model.ItineraryItem
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "NotificationScheduler"
    }

    /**
     * Programa una notificación para un item del itinerario
     */
    fun scheduleNotification(itineraryItem: ItineraryItem) {
        if (!itineraryItem.hasReminder) {
            Log.d(TAG, "Item ${itineraryItem.id} no tiene recordatorio activado")
            return
        }

        val notificationTime = calculateNotificationTime(
            itineraryItem.activityDateTime,
            itineraryItem.reminderTimeBefore
        )

        // No programar si el tiempo ya pasó
        if (notificationTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Tiempo de notificación ya pasó para item ${itineraryItem.id}")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_ITINERARY_ID, itineraryItem.id)
            putExtra(NotificationHelper.EXTRA_TRIP_ID, itineraryItem.tripId)
            putExtra(NotificationHelper.EXTRA_DESCRIPTION, itineraryItem.description)
            putExtra(NotificationHelper.EXTRA_ALERT_TYPE, itineraryItem.alertType.name)
            putExtra("activity_time", itineraryItem.activityDateTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itineraryItem.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Usar setExactAndAllowWhileIdle para Android 6.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Notificación programada para item ${itineraryItem.id} a las ${java.util.Date(notificationTime)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error al programar notificación: ${e.message}")
        }
    }

    /**
     * Cancela una notificación programada
     */
    fun cancelNotification(itineraryId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itineraryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        
        // También cancelar la notificación si ya está mostrada
        val notificationHelper = NotificationHelper(context)
        notificationHelper.cancelNotification(itineraryId)
        
        Log.d(TAG, "Notificación cancelada para item $itineraryId")
    }

    /**
     * Reprograma todas las notificaciones (útil después de reiniciar el dispositivo)
     */
    fun rescheduleNotification(itineraryItem: ItineraryItem) {
        cancelNotification(itineraryItem.id)
        scheduleNotification(itineraryItem)
    }

    /**
     * Calcula el tiempo de la notificación basado en el tiempo de la actividad
     * y el tiempo antes especificado
     */
    private fun calculateNotificationTime(activityDateTime: Long, reminderTimeBefore: String): Long {
        val minutesBefore = when (reminderTimeBefore) {
            "15 minutos antes" -> 15L
            "30 minutos antes" -> 30L
            "1 hora antes" -> 60L
            "2 horas antes" -> 120L
            "1 día antes" -> 1440L
            "2 días antes" -> 2880L
            else -> 60L // Por defecto 1 hora
        }

        return activityDateTime - TimeUnit.MINUTES.toMillis(minutesBefore)
    }

    /**
     * Verifica si se pueden programar alarmas exactas (Android 12+)
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}

