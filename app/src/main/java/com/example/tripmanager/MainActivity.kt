package com.example.tripmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tripmanager.navigation.TripNavigation
import com.example.tripmanager.notification.NotificationHelper
import com.example.tripmanager.notification.PermissionHelper
import com.example.tripmanager.ui.theme.TripManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Solicitar permiso de notificaciones al iniciar la app
        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this)
        }
        
        // Inicializar canales de notificación
        NotificationHelper(this)
        
        setContent {
            TripManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TripNavigation()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripManagerPreview() {
    TripManagerTheme {
        TripNavigation()
    }
}