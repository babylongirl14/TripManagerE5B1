package com.example.tripmanager.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tripmanager.MainActivity
import com.example.tripmanager.R
import com.example.tripmanager.data.model.AlertType

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID_NORMAL = "itinerary_reminders_normal"
        const val CHANNEL_ID_IMPORTANT = "itinerary_reminders_important"
        const val CHANNEL_NAME_NORMAL = "Recordatorios de Itinerario"
        const val CHANNEL_NAME_IMPORTANT = "Recordatorios Importantes"
        
        const val EXTRA_ITINERARY_ID = "itinerary_id"
        const val EXTRA_TRIP_ID = "trip_id"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_ALERT_TYPE = "alert_type"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para notificaciones normales
            val channelNormal = NotificationChannel(
                CHANNEL_ID_NORMAL,
                CHANNEL_NAME_NORMAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de actividades del itinerario"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            // Canal para notificaciones importantes
            val channelImportant = NotificationChannel(
                CHANNEL_ID_IMPORTANT,
                CHANNEL_NAME_IMPORTANT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios importantes de actividades del itinerario"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelNormal)
            notificationManager.createNotificationChannel(channelImportant)
        }
    }

    fun showNotification(
        itineraryId: Long,
        tripId: Long,
        description: String,
        alertType: AlertType,
        activityTime: Long
    ) {
        val channelId = if (alertType == AlertType.IMPORTANT) {
            CHANNEL_ID_IMPORTANT
        } else {
            CHANNEL_ID_NORMAL
        }

        // Intent para abrir la app cuando se toque la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_ITINERARY_ID, itineraryId)
            putExtra(EXTRA_TRIP_ID, tripId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            itineraryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (alertType == AlertType.IMPORTANT) {
            "⚠️ Recordatorio Importante"
        } else {
            "📅 Recordatorio de Actividad"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(
                if (alertType == AlertType.IMPORTANT) {
                    NotificationCompat.PRIORITY_HIGH
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(
                if (alertType == AlertType.IMPORTANT) {
                    longArrayOf(0, 500, 250, 500)
                } else {
                    longArrayOf(0, 250, 250, 250)
                }
            )
            .build()

        // Verificar permisos antes de mostrar la notificación
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    NotificationManagerCompat.from(context).notify(itineraryId.toInt(), notification)
                }
            } else {
                NotificationManagerCompat.from(context).notify(itineraryId.toInt(), notification)
            }
        } catch (e: SecurityException) {
            // Permiso denegado, no se puede mostrar la notificación
            android.util.Log.w("NotificationHelper", "No se pudo mostrar la notificación: permiso denegado")
        }
    }

    fun cancelNotification(itineraryId: Long) {
        NotificationManagerCompat.from(context).cancel(itineraryId.toInt())
    }
}

