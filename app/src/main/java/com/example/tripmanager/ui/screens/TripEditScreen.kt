package com.example.tripmanager.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripmanager.R
import com.example.tripmanager.data.database.TripDatabase
import com.example.tripmanager.data.model.TripType
import com.example.tripmanager.data.repository.TripRepository
import com.example.tripmanager.data.session.UserSession
import com.example.tripmanager.ui.viewmodel.TripEditViewModel
import java.text.SimpleDateFormat
import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripEditScreen(
    tripId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { TripDatabase.getDatabase(context) }
    val tripRepository = remember { TripRepository(database.tripDao()) }
    val tripEditViewModel = remember { TripEditViewModel(
        tripRepository,
        UserSession.getCurrentUser() ?: "default_user",
        tripId
    ) }
    val uiState by tripEditViewModel.uiState.collectAsState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

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
                        text = "Viaje",
                        fontSize = 20.sp,
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Destination Field
                        OutlinedTextField(
                            value = uiState.destination,
                            onValueChange = tripEditViewModel::onDestinationChange,
                            label = { Text("Destino") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF333333),
                                unfocusedTextColor = Color(0xFF333333)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Start Date Field - MEJORADO PARA TODA EL ÁREA CLICKEABLE
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStartDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(uiState.startDate),
                                onValueChange = { },
                                label = { Text("Fecha de inicio") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = false, // Deshabilitar para evitar interferencias con el clickable del Box
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color(0xFF333333),
                                    disabledLabelColor = Color(0xFF666666),
                                    disabledBorderColor = Color(0xFF666666),
                                    disabledLeadingIconColor = Color(0xFF666666),
                                    disabledTrailingIconColor = Color(0xFF666666)
                                ),
                                trailingIcon = {
                                    Text(
                                        text = "📅",
                                        fontSize = 20.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // End Date Field - MEJORADO PARA TODA EL ÁREA CLICKEABLE
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(uiState.endDate),
                                onValueChange = { },
                                label = { Text("Fecha de fin") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = false, // Deshabilitar para evitar interferencias con el clickable del Box
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color(0xFF333333),
                                    disabledLabelColor = Color(0xFF666666),
                                    disabledBorderColor = Color(0xFF666666),
                                    disabledLeadingIconColor = Color(0xFF666666),
                                    disabledTrailingIconColor = Color(0xFF666666)
                                ),
                                trailingIcon = {
                                    Text(
                                        text = "📅",
                                        fontSize = 20.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trip Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = if (uiState.tripType == TripType.VACATION) "Vacaciones" else "Trabajo",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Tipo de viaje") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF333333),
                                    unfocusedTextColor = Color(0xFF333333)
                                ),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Vacaciones") },
                                    onClick = {
                                        tripEditViewModel.onTripTypeChange(TripType.VACATION)
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Trabajo") },
                                    onClick = {
                                        tripEditViewModel.onTripTypeChange(TripType.WORK)
                                        expanded = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Error Message
                        uiState.errorMessage?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Save Button
                        Button(
                            onClick = tripEditViewModel::saveTrip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            enabled = !uiState.isLoading
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
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            selectedDate = uiState.startDate,
            onDateSelected = { date ->
                tripEditViewModel.onStartDateChange(date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            selectedDate = uiState.endDate,
            onDateSelected = { date ->
                tripEditViewModel.onEndDateChange(date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localMidnightOffset = TimeZone.getDefault().getOffset(millis)
                        val correctedDate = Date(millis + localMidnightOffset)
                        onDateSelected(correctedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


