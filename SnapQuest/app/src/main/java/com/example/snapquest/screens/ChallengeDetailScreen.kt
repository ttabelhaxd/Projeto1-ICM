package com.example.snapquest.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.snapquest.manages.LocationManager
import com.example.snapquest.models.Challenge
import com.example.snapquest.ui.components.*
import com.example.snapquest.utils.ImagePickerUtils
import com.example.snapquest.utils.calculateDistance
import com.example.snapquest.utils.formatDate
import com.example.snapquest.viewModels.QuestUiState
import com.example.snapquest.viewModels.QuestViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailsScreen(
    questId: String,
    challengeId: String,
    navController: NavHostController,
    viewModel: QuestViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val challenge by viewModel.getChallengeById(questId, challengeId).collectAsState(initial = null)
    val user by viewModel.currentUser.collectAsState()
    val userQuest by viewModel.getUserQuest(user?.uid ?: "", questId).collectAsState(initial = null)
    val userPhotoUrl = userQuest?.completedPhotos?.get(challengeId)

    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val isCompleted = userQuest?.completedChallenges?.contains(challengeId) ?: false
    val canComplete = user != null && !isCompleted
    val isLoading by viewModel.isLoading.collectAsState()
    var showImageDialog by remember { mutableStateOf(false) }
    var showUserImageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is QuestUiState.ChallengeCompleted -> {
                viewModel.fetchUserQuests()
                navController.popBackStack()
            }
            is QuestUiState.Error -> {
                verificationMessage = (uiState as QuestUiState.Error).message
                showVerificationDialog = true
            }
            else -> {}
        }
    }

    // Launcher para a câmera
    val imagePicker = ImagePickerUtils.rememberImagePicker(
        onImageSelected = { imagePath ->
            // Verificar localização após a foto ser tirada/selecionada
            viewModel.viewModelScope.launch {
                verifyLocationAndComplete(
                    challenge = challenge,
                    imageUri = Uri.parse(imagePath),
                    viewModel = viewModel,
                    userId = user?.uid ?: return@launch,
                    questId = questId,
                    challengeId = challengeId,
                    setMessage = { verificationMessage = it },
                    setShowDialog = { showVerificationDialog = it }
                )
            }
        },
        onError = { errorMessage ->
            verificationMessage = errorMessage
            showVerificationDialog = true
        }
    )

    // Launcher para permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.viewModelScope.launch {
                getCurrentLocation { location ->
                    currentLocation = location
                }
            }
        }
    }

    // Efeito para carregar a localização atual
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation { location ->
                currentLocation = location
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge?.name ?: "Challenge Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                challenge?.let { ch ->
                    // Nome do desafio
                    Text(
                        text = ch.name,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Mapa com a localização do desafio
                    ChallengeLocationMap(
                        challengeLocation = LatLng(ch.latitude, ch.longitude),
                        currentLocation = currentLocation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 16.dp)
                    )

                    // Distância atual (se tivermos localização)
                    currentLocation?.let { userLoc ->
                        val distance = calculateDistance(
                            userLoc.latitude,
                            userLoc.longitude,
                            ch.latitude,
                            ch.longitude
                        )
                        Text(
                            text = "Distance: ${"%.2f".format(distance)} meters",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Descrição (só aparece quando completado)
                    if (isCompleted) {
                        Text(
                            text = ch.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Dica (sempre visível)
                    if (ch.hint.isNotEmpty()) {
                        Text(
                            text = "Hint: ${ch.hint}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (showImageDialog) {
                        Dialog(
                            onDismissRequest = { showImageDialog = false }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    QuestImageLoader(
                                        imageUrl = ch.hintPhotoUrl,
                                        contentDescription = "Fullscreen challenge hint image",
                                        modifier = Modifier.fillMaxWidth().height(500.dp)
                                    )

                                    IconButton(
                                        onClick = { showImageDialog = false },
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Foto de dica (se existir)
                    if (ch.hintPhotoUrl.isNotEmpty()) {
                        Text(
                            text = "Hint Photo:",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 8.dp)
                                .clickable { showImageDialog = true }
                        ){
                            QuestImageLoader(
                                imageUrl = ch.hintPhotoUrl,
                                contentDescription = "Challenge hint image",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    // Foto tirada pelo utilizador (se completado)
                    if (isCompleted && userPhotoUrl != null) {
                        if (showUserImageDialog) {
                            Dialog(
                                onDismissRequest = { showUserImageDialog = false }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(500.dp),
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        QuestImageLoader(
                                            imageUrl = userPhotoUrl,
                                            contentDescription = "Fullscreen User image",
                                            modifier = Modifier.fillMaxWidth().height(500.dp)
                                        )

                                        IconButton(
                                            onClick = { showUserImageDialog = false },
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            text = "Your Photo:",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showUserImageDialog = true }
                        ){
                            QuestImageLoader(
                                imageUrl = userPhotoUrl,
                                contentDescription = "User completed challenge photo",
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    // Botão para completar
                    if (canComplete) {
                        Column {
                            Button(
                                onClick = {
                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    } else {
                                        // Usar o imagePicker para criar arquivo e lançar câmera
                                        val photoFile = imagePicker.createImageFile()
                                        imagePicker.launchCamera(photoFile)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Take Photo to Complete")
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else if (isCompleted) {
                        Text(
                            text = "Completed on ${userQuest?.completedAt?.let { formatDate(it) }}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // Diálogo de verificação
                if (showVerificationDialog) {
                    AlertDialog(
                        onDismissRequest = { showVerificationDialog = false },
                        title = { Text("Verification") },
                        text = { Text(verificationMessage ?: "") },
                        confirmButton = {
                            TextButton(
                                onClick = { showVerificationDialog = false }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeLocationMap(
    challengeLocation: LatLng,
    currentLocation: LatLng?,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(challengeLocation, 15f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false
        )
    ) {
        // Marcador do desafio
        Marker(
            state = MarkerState(position = challengeLocation),
            title = "Challenge Location",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        )

        // Marcador da localização atual (se disponível)
        currentLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Your Location",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }
    }
}

private suspend fun getCurrentLocation(onSuccess: (LatLng) -> Unit) {
    LocationManager.getLastLocation()?.let { location ->
        onSuccess(LatLng(location.latitude, location.longitude))
    }
}

private suspend fun verifyLocationAndComplete(
    challenge: Challenge?,
    imageUri: Uri,
    viewModel: QuestViewModel,
    userId: String,
    questId: String,
    challengeId: String,
    setMessage: (String) -> Unit,
    setShowDialog: (Boolean) -> Unit
) {
    if (challenge == null) return

    LocationManager.getLastLocation()?.let { userLocation ->
        val distance = calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            challenge.latitude,
            challenge.longitude
        )

        if (distance <= 10) { // 10 metros de distância permitida
            viewModel.completeChallengeWithPhoto(
                userId = userId,
                questId = questId,
                challengeId = challengeId,
                photoPath = imageUri.toString()
            )
        } else {
            setMessage("You're too far from the challenge location (${"%.2f".format(distance)}m). Get closer to complete it!")
            setShowDialog(true)
        }
    } ?: run {
        setMessage("Could not verify your location. Please enable location services.")
        setShowDialog(true)
    }
}