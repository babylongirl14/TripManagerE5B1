package com.example.tripmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.tripmanager.R
import java.util.Calendar
import com.example.tripmanager.data.database.TripDatabase
import com.example.tripmanager.data.model.ItineraryItem
import com.example.tripmanager.data.model.AlertType
import com.example.tripmanager.data.repository.ItineraryRepository
import com.example.tripmanager.data.repository.TripRepository
import com.example.tripmanager.ui.viewmodel.ItineraryEditViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryEditScreen(
    tripId: Long,
    itineraryId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { TripDatabase.getDatabase(context) }
    val itineraryRepository = remember { ItineraryRepository(database.itineraryDao()) }
    val tripRepository = remember { TripRepository(database.tripDao()) }
    val itineraryEditViewModel = remember { 
        ItineraryEditViewModel(
            itineraryRepository,
            tripRepository,
            tripId,
            itineraryId
        ) 
    }
    val uiState by itineraryEditViewModel.uiState.collectAsState()
    var showDateTimePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var reminderExpanded by remember { mutableStateOf(false) }
    
    // Date and time picker state
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }

    // Initialize date and time from current state
    LaunchedEffect(uiState.activityDateTime) {
        if (uiState.activityDateTime > 0) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = uiState.activityDateTime
            selectedDate = calendar
            selectedTime = calendar
        }
    }

    LaunchedEffect(uiState.isSaveSuccessful) {
        if (uiState.isSaveSuccessful) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (itineraryId == null) "Nueva Actividad" else "Editar Actividad",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fecha y hora
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Fecha y hora de la actividad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = uiState.activityDateTimeText,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Fecha y hora") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF333333),
                                unfocusedTextColor = Color(0xFF333333)
                            ),
                            trailingIcon = {
                                TextButton(onClick = { showDateTimePicker = true }) {
                                    Text("Seleccionar")
                                }
                            }
                        )
                    }
                }

                // Descripción
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Descripción de la actividad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { itineraryEditViewModel.onDescriptionChange(it) },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF333333),
                                unfocusedTextColor = Color(0xFF333333)
                            ),
                            supportingText = {
                                Text("${uiState.description.length}/200")
                            },
                            isError = uiState.description.length > 200
                        )
                    }
                }

                // Recordatorio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "¿Agregar recordatorio?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.hasReminder,
                                    onClick = { itineraryEditViewModel.onHasReminderChange(true) }
                                )
                                Text("Sí")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !uiState.hasReminder,
                                    onClick = { itineraryEditViewModel.onHasReminderChange(false) }
                                )
                                Text("No")
                            }
                        }

                        if (uiState.hasReminder) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ExposedDropdownMenuBox(
                                expanded = reminderExpanded,
                                onExpandedChange = { reminderExpanded = !reminderExpanded }
                            ) {
                                OutlinedTextField(
                                    value = uiState.reminderTimeBefore,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Tiempo antes") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF333333),
                                        unfocusedTextColor = Color(0xFF333333)
                                    ),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = reminderExpanded,
                                    onDismissRequest = { reminderExpanded = false }
                                ) {
                                    listOf(
                                        "30 minutos antes",
                                        "1 hora antes",
                                        "2 horas antes",
                                        "3 horas antes",
                                        "1 día antes",
                                        "2 días antes",
                                        "3 días antes"
                                    ).forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                itineraryEditViewModel.onReminderTimeChange(option)
                                                reminderExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tipo de alerta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tipo de alerta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.alertType == AlertType.IMPORTANT,
                                    onClick = { itineraryEditViewModel.onAlertTypeChange(AlertType.IMPORTANT) }
                                )
                                Text("Importante")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.alertType == AlertType.NORMAL,
                                    onClick = { itineraryEditViewModel.onAlertTypeChange(AlertType.NORMAL) }
                                )
                                Text("Normal")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón guardar
                Button(
                    onClick = { itineraryEditViewModel.saveItineraryItem() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    enabled = uiState.isValid && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Guardar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    // Date and Time Picker Dialog
    if (showDateTimePicker) {
        LaunchedEffect(showDateTimePicker) {
            if (showDateTimePicker) {
                // Show date picker first
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate.set(year, month, dayOfMonth)
                        
                        // Then show time picker
                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                selectedTime.set(Calendar.MINUTE, minute)
                                
                                // Combine date and time
                                val combinedCalendar = Calendar.getInstance()
                                combinedCalendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                                combinedCalendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                                combinedCalendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                                combinedCalendar.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                                combinedCalendar.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                                combinedCalendar.set(Calendar.SECOND, 0)
                                combinedCalendar.set(Calendar.MILLISECOND, 0)
                                
                                itineraryEditViewModel.onActivityDateTimeChange(combinedCalendar.timeInMillis)
                                showDateTimePicker = false
                            },
                            selectedTime.get(Calendar.HOUR_OF_DAY),
                            selectedTime.get(Calendar.MINUTE),
                            true
                        )
                        timePickerDialog.show()
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            }
        }
    }
}
