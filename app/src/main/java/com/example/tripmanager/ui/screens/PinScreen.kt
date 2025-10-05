package com.example.tripmanager.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.tripmanager.R
import com.example.tripmanager.data.database.TripDatabase
import com.example.tripmanager.data.repository.UserRepository
import com.example.tripmanager.data.security.PinManager
import com.example.tripmanager.data.session.UserSession
import com.example.tripmanager.ui.viewmodel.PinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    tripId: Long,
    onNavigateBack: () -> Unit,
    onPinSuccess: () -> Unit,
    onNavigateToReset: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { TripDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val pinManager = remember { PinManager(context) }
    val viewModel = remember { PinViewModel(userRepository, pinManager) }
    
    val uiState by viewModel.uiState.collectAsState()
    val isFirstTime = !uiState.hasPin && !uiState.isPinSet
    val isConfirming = uiState.isConfirming

    LaunchedEffect(uiState.isPinVerified) {
        if (uiState.isPinVerified) {
            onPinSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            isFirstTime && !isConfirming -> "Crear PIN"
                            isFirstTime && isConfirming -> "Confirmar PIN"
                            uiState.isLocked -> "PIN Bloqueado"
                            else -> "Ingresar PIN"
                        },
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
                text = when {
                    isFirstTime && !isConfirming -> "Establece tu PIN de 4 dígitos"
                    isFirstTime && isConfirming -> "Confirma tu nuevo PIN"
                    uiState.isLocked -> "Demasiados intentos. Restablece tu PIN."
                    else -> "Ingresa tu PIN de 4 dígitos"
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

            if (uiState.isLocked) {
                Button(
                    onClick = onNavigateToReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Restablecer PIN")
                }
            } else {
                NumberPad(
                    onDigitClick = viewModel::onPinDigitEntered,
                    onBackspaceClick = viewModel::onBackspaceClick,
                    canAttemptPin = uiState.currentPin.length < 4
                )
            }
        }
    }
}

@Composable
fun PinInputDisplay(pin: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            PinDot(isActive = index < pin.length)
        }
    }
}

@Composable
fun PinDot(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(if (isActive) Color(0xFF4CAF50) else Color(0xFFCCCCCC))
    )
}

@Composable
fun NumberPad(
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

@Composable
fun NumberPadButton(digit: String, onClick: (String) -> Unit) {
    Button(
        onClick = { onClick(digit) },
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = digit,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinResetScreen(
    onPinResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { TripDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val pinManager = remember { PinManager(context) }
    val viewModel = remember { PinViewModel(userRepository, pinManager) }
    
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf(UserSession.getCurrentUser() ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var resetStep by remember { mutableStateOf(ResetStep.VALIDATE_LOGIN) }
    var newPin by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isLoginValidatedForReset) {
        if (uiState.isLoginValidatedForReset && resetStep == ResetStep.VALIDATE_LOGIN) {
            resetStep = ResetStep.CREATE_NEW_PIN
            viewModel.clearCurrentPin()
        }
    }

    LaunchedEffect(uiState.isPinSet) {
        if (uiState.isPinSet && resetStep == ResetStep.CONFIRM_NEW_PIN) {
            onPinResetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (resetStep) {
                            ResetStep.VALIDATE_LOGIN -> "Restablecer PIN"
                            ResetStep.CREATE_NEW_PIN -> "Nuevo PIN"
                            ResetStep.CONFIRM_NEW_PIN -> "Confirmar PIN"
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
        when (resetStep) {
            ResetStep.VALIDATE_LOGIN -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFF5F5F5))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ingresa tu contraseña de login para restablecer el PIN",
                        fontSize = 16.sp,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    uiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.validateLoginForReset(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Validar y Restablecer")
                        }
                    }
                }
            }
            
            ResetStep.CREATE_NEW_PIN, ResetStep.CONFIRM_NEW_PIN -> {
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
                        text = when (resetStep) {
                            ResetStep.CREATE_NEW_PIN -> "Ingresa tu nuevo PIN de 4 dígitos"
                            ResetStep.CONFIRM_NEW_PIN -> "Confirma tu nuevo PIN"
                            else -> ""
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

                    NumberPadForReset(
                        onDigitClick = { digit ->
                            if (uiState.currentPin.length < 4) {
                                val newCurrentPin = uiState.currentPin + digit
                                viewModel.updateCurrentPin(newCurrentPin)
                                if (newCurrentPin.length == 4) {
                                    when (resetStep) {
                                        ResetStep.CREATE_NEW_PIN -> {
                                            newPin = newCurrentPin
                                            resetStep = ResetStep.CONFIRM_NEW_PIN
                                            viewModel.clearCurrentPin()
                                        }
                                        ResetStep.CONFIRM_NEW_PIN -> {
                                            if (newCurrentPin == newPin) {
                                                viewModel.changePin(newPin)
                                            } else {
                                                viewModel.showError("Los PINs no coinciden")
                                                resetStep = ResetStep.CREATE_NEW_PIN
                                                viewModel.clearCurrentPin()
                                            }
                                        }
                                        else -> {}
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
    }
}

enum class ResetStep {
    VALIDATE_LOGIN,
    CREATE_NEW_PIN,
    CONFIRM_NEW_PIN
}

@Composable
fun NumberPadForReset(
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