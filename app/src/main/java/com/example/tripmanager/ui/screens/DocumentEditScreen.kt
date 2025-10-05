package com.example.tripmanager.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripmanager.R
import com.example.tripmanager.data.database.TripDatabase
import com.example.tripmanager.data.model.DocumentType
import com.example.tripmanager.data.repository.DocumentRepository
import com.example.tripmanager.ui.viewmodel.DocumentEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditScreen(
    tripId: Long,
    documentId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { TripDatabase.getDatabase(context) }
    val documentRepository = remember { DocumentRepository(database.documentDao()) }
    val documentEditViewModel = remember { DocumentEditViewModel(documentRepository, tripId, documentId) }
    
    val uiState by documentEditViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaveSuccessful) {
        if (uiState.isSaveSuccessful) {
            onNavigateBack()
        }
    }

    // File picker launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            documentEditViewModel.onFileSelected(it, fileName, context)
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "document_${System.currentTimeMillis()}.pdf"
            documentEditViewModel.onFileSelected(it, fileName, context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (documentId != null) "Editar Documento" else "Agregar Documento",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Información del Documento",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = uiState.title,
                                onValueChange = documentEditViewModel::onTitleChange,
                                label = { Text("Título del documento") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF333333),
                                    unfocusedTextColor = Color(0xFF333333)
                                )
                            )
                        }
                    }

                    // File Upload Section
                    if (documentId == null) { // Only show file upload for new documents
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Subir Archivo",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Image Upload Section
                                FileUploadSection(
                                    title = "Subir Imagen",
                                    description = "JPG, PNG, GIF, etc.",
                                    icon = Icons.Default.DateRange,
                                    backgroundColor = Color(0xFFE3F2FD),
                                    iconColor = Color(0xFF2196F3),
                                    onClick = { imagePickerLauncher.launch("image/*") }
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // PDF Upload Section
                                FileUploadSection(
                                    title = "Subir PDF",
                                    description = "Documentos PDF",
                                    icon = Icons.Default.Star,
                                    backgroundColor = Color(0xFFE8F5E8),
                                    iconColor = Color(0xFF4CAF50),
                                    onClick = { documentPickerLauncher.launch("application/pdf") }
                                )
                                
                                // Selected file info
                                if (uiState.selectedFileName.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFF0F8F0)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (uiState.documentType == "image") 
                                                    Icons.Default.DateRange else Icons.Default.Star,
                                                contentDescription = "File type",
                                                tint = Color(0xFF4CAF50)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Column {
                                                Text(
                                                    text = "Archivo seleccionado:",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF666666)
                                                )
                                                Text(
                                                    text = uiState.selectedFileName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF333333)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Error Message
                    uiState.errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Save Button
                    Button(
                        onClick = { documentEditViewModel.saveDocument(context) },
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
                                text = "Guardar Documento",
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

@Composable
fun FileUploadSection(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Upload",
                tint = Color(0xFF666666)
            )
        }
    }
}
