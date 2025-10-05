package com.example.tripmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.tripmanager.data.database.TripDatabase
import com.example.tripmanager.data.repository.UserRepository
import com.example.tripmanager.data.security.PinManager
import com.example.tripmanager.ui.viewmodel.PinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinScreen(
    onNavigateBack: () -> Unit,
    onPinChangeSuccess: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { TripDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val pinManager = remember { PinManager(context) }
    val viewModel = remember { PinViewModel(userRepository, pinManager) }
    
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(ChangePinStep.VERIFY_CURRENT) }
    var newPin by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isPinVerified) {
        if (uiState.isPinVerified && currentStep == ChangePinStep.VERIFY_CURRENT) {
            currentStep = ChangePinStep.ENTER_NEW
        }
    }

    LaunchedEffect(uiState.isPinSet) {
        if (uiState.isPinSet && currentStep == ChangePinStep.CONFIRM_NEW) {
            onPinChangeSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentStep) {
                            ChangePinStep.VERIFY_CURRENT -> "PIN Actual"
                            ChangePinStep.ENTER_NEW -> "Nuevo PIN"
                            ChangePinStep.CONFIRM_NEW -> "Confirmar PIN"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = when (currentStep) {
                    ChangePinStep.VERIFY_CURRENT -> "Ingresa tu PIN actual"
                    ChangePinStep.ENTER_NEW -> "Ingresa tu nuevo PIN"
                    ChangePinStep.CONFIRM_NEW -> "Confirma tu nuevo PIN"
                },
                fontSize = 18.sp,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            PinInputDisplay(pin = uiState.currentPin)

            Spacer(modifier = Modifier.height(32.dp))

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            NumberPadForChangePin(
                onDigitClick = { digit ->
                    when (currentStep) {
                        ChangePinStep.VERIFY_CURRENT -> {
                            viewModel.onPinDigitEntered(digit)
                        }
                        ChangePinStep.ENTER_NEW -> {
                            if (uiState.currentPin.length < 4) {
                                val newCurrentPin = uiState.currentPin + digit
                                viewModel.updateCurrentPin(newCurrentPin)
                                if (newCurrentPin.length == 4) {
                                    newPin = newCurrentPin
                                    currentStep = ChangePinStep.CONFIRM_NEW
                                    viewModel.clearCurrentPin()
                                }
                            }
                        }
                        ChangePinStep.CONFIRM_NEW -> {
                            if (uiState.currentPin.length < 4) {
                                val confirmPin = uiState.currentPin + digit
                                viewModel.updateCurrentPin(confirmPin)
                                if (confirmPin.length == 4) {
                                    if (confirmPin == newPin) {
                                        viewModel.changePin(newPin)
                                    } else {
                                        viewModel.showError("Los PINs no coinciden")
                                        currentStep = ChangePinStep.ENTER_NEW
                                        viewModel.clearCurrentPin()
                                    }
                                }
                            }
                        }
                    }
                },
                onBackspaceClick = viewModel::onBackspaceClick,
                canAttemptPin = uiState.currentPin.length < 4
            )
        }
    }
}

@Composable
fun NumberPadForChangePin(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    canAttemptPin: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        (1..9).chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { digit ->
                    NumberPadButton(digit = digit.toString()) { onDigitClick(it) }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumberPadButton(digit = "0") { onDigitClick(it) }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable(enabled = canAttemptPin) { onBackspaceClick() }
                    .background(Color(0xFFE8F5E8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌫",
                    fontSize = 20.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

enum class ChangePinStep {
    VERIFY_CURRENT,
    ENTER_NEW,
    CONFIRM_NEW
}


