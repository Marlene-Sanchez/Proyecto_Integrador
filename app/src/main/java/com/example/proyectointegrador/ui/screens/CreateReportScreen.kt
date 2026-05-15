package com.example.proyectointegrador.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.proyectointegrador.ui.viewmodel.ReportViewModel
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) imageUri = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                location = Pair(lat, lon)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Reporte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                // Crear archivo temporal y lanzar la cámara
                val directory = File(context.cacheDir, "images")
                directory.mkdirs()
                val file = File.createTempFile("report_", ".jpg", directory)
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)
                currentPhotoFile = file
                imageUri = uri
                cameraLauncher.launch(uri)
            }) {
                Text("Tomar Foto")
            }

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
            }

            Button(onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) {
                Text(if (location == null) "Obtener Ubicación" else "Ubicación: ${location!!.first}, ${location!!.second}")
            }

            Button(
                onClick = {
                    val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val latPart = location?.first?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()) ?: "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                    val lonPart = location?.second?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()) ?: "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    val imagePart = currentPhotoFile?.let { file ->
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", file.name, requestFile)
                    }

                    viewModel.createReport(titlePart, descPart, latPart, lonPart, imagePart) {
                        onNavigateBack()
                    }
                },
                enabled = title.isNotBlank() && location != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar Reporte")
            }
        }
    }
}

// Nota: ahora usamos currentPhotoFile para construir el MultipartBody.Part

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
        if (loc != null) {
            onLocationReceived(loc.latitude, loc.longitude)
        }
    }
}
