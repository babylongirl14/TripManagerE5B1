package com.example.tripmanager.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.tripmanager.R
import com.example.tripmanager.data.database.TripDatabase
import com.example.tripmanager.data.model.ItineraryItem
import com.example.tripmanager.data.model.AlertType
import com.example.tripmanager.data.repository.ItineraryRepository
import com.example.tripmanager.ui.viewmodel.ItineraryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryListScreen(
    tripId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddItinerary: () -> Unit,
    onNavigateToEditItinerary: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = remember { TripDatabase.getDatabase(context) }
    val itineraryRepository = remember { ItineraryRepository(database.itineraryDao(), context) }
    val itineraryViewModel = remember { ItineraryViewModel(itineraryRepository, tripId) }
    val uiState by itineraryViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Itinerario",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    // Small logo in top right
                    Image(
                        painter = painterResource(id = R.drawable.log),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp)), // opcional, esquinas redondeadas
                        contentScale = ContentScale.Crop
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddItinerary,
                containerColor = Color(0xFF1DE9B6)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Activity",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.itinerary.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No tienes actividades aún",
                        fontSize = 18.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca el botón + para agregar tu primera actividad",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.itinerary) { item ->
                        ItineraryCard(
                            item = item,
                            onEditClick = { onNavigateToEditItinerary(item.id) },
                            onDeleteClick = { itineraryViewModel.deleteItineraryItem(item) }
                        )
                    }
                }
            }

            // Error message
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.9f))
                ) {
                    Text(
                        text = error,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ItineraryCard(
    item: ItineraryItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val activityDateTime = dateFormat.format(Date(item.activityDateTime))
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activityDateTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (item.hasReminder) {
                    Text(
                        text = "Recordatorio: ${item.reminderTimeBefore}",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                }
                
                Text(
                    text = if (item.alertType == AlertType.IMPORTANT) "Importante" else "Normal",
                    fontSize = 12.sp,
                    color = if (item.alertType == AlertType.IMPORTANT) Color(0xFFE57373) else Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Box {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Activity Options",
                        tint = Color(0xFF666666)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Editar")
                            }
                        },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Eliminar", color = Color(0xFFE57373))
                            }
                        },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
            
            // Diálogo de confirmación para eliminar
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(
                            text = "Eliminar actividad",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text("¿Estás seguro de que quieres eliminar esta actividad? Esta acción no se puede deshacer.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                onDeleteClick()
                            }
                        ) {
                            Text(
                                text = "Eliminar",
                                color = Color(0xFFE57373)
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
