package com.example.snapquest.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.manages.LocationManager
import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.example.snapquest.ui.components.CustomDatePicker
import com.example.snapquest.ui.components.LocationPickerDialog
import com.example.snapquest.ui.components.LocationSelector
import com.example.snapquest.ui.components.PhotoUploader
import com.example.snapquest.viewModels.QuestUiState
import com.example.snapquest.viewModels.QuestViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuestScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: QuestViewModel
) {
    var questName by remember { mutableStateOf("") }
    var questDescription by remember { mutableStateOf("") }
    var questImageUrl by remember { mutableStateOf("") }
    var questStartDate by remember { mutableStateOf(Date()) }
    var questEndDate by remember { mutableStateOf(Date()) }
    var questLatitude by remember { mutableStateOf("") }
    var questLongitude by remember { mutableStateOf("") }
    var questChallenges by remember { mutableStateOf(listOf<Challenge>()) }
    var questPhotoPath by remember { mutableStateOf("") }
    var challengePhotoPaths by remember { mutableStateOf<List<String>>(emptyList()) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var showQuestLocationPicker by remember { mutableStateOf(false) }
    var showChallengeLocationPickerIndex by remember { mutableIntStateOf(-1) }

    // Fecha a tela automaticamente após criar a quest
    LaunchedEffect(uiState) {
        when (uiState) {
            is QuestUiState.QuestCreated -> {
                Toast.makeText(context, "Quest created successfully", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is QuestUiState.Error -> {
                Toast.makeText(context, (uiState as QuestUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        LocationManager.initialize(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Quest") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        enabled = uiState != QuestUiState.Loading
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Seção de informações básicas
            Text(
                text = "Quest Information",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Campo para nome da quest
            OutlinedTextField(
                value = questName,
                onValueChange = { questName = it },
                label = { Text("Quest Name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                isError = questName.isBlank()
            )

            // Campo para descrição
            OutlinedTextField(
                value = questDescription,
                onValueChange = { questDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                maxLines = 3
            )

            // Campo para upload da imagem
            Text(
                text = "Quest Photo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            PhotoUploader(
                currentPhotoPath = questPhotoPath,
                onPhotoSelected = { path ->
                    questPhotoPath = path
                    coroutineScope.launch {
                        questPhotoPath = ""
                        delay(100)
                        questPhotoPath = path
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Seção de localização
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            LocationSelector(
                latitude = questLatitude,
                longitude = questLongitude,
                onSelectLocation = { showQuestLocationPicker = true },
                onUseCurrentLocation = {
                    coroutineScope.launch {
                        val location = LocationManager.getLastLocation()
                        location?.let {
                            questLatitude = it.latitude.toString()
                            questLongitude = it.longitude.toString()
                        } ?: run {
                            Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (showQuestLocationPicker) {
                val initialLocation = if (questLatitude.isNotBlank() && questLongitude.isNotBlank()) {
                    LatLng(questLatitude.toDouble(), questLongitude.toDouble())
                } else {
                    null
                }

                LocationPickerDialog(
                    initialLocation = initialLocation,
                    onConfirm = { latLng ->
                        questLatitude = latLng.latitude.toString()
                        questLongitude = latLng.longitude.toString()
                        showQuestLocationPicker = false
                    },
                    onDismiss = { showQuestLocationPicker = false }
                )
            }
            // Seção de datas
            Text(
                text = "Dates",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                // Date picker simplificado (poderia ser melhorado com um DatePickerDialog)
                CustomDatePicker(
                    selectedDate = questStartDate,
                    onDateSelected = { questStartDate = it },
                    label = "Start Date",
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                CustomDatePicker(
                    selectedDate = questEndDate,
                    onDateSelected = { questEndDate = it },
                    label = "End Date",
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
            }

            // Seção de desafios
            Text(
                text = "Challenges",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            questChallenges.forEachIndexed { index, challenge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Challenge ${index + 1}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = challenge.name,
                            onValueChange = { newName ->
                                questChallenges = questChallenges.toMutableList().apply {
                                    this[index] = this[index].copy(name = newName)
                                }
                            },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = challenge.description,
                            onValueChange = { newDescription ->
                                questChallenges = questChallenges.toMutableList().apply {
                                    this[index] = this[index].copy(description = newDescription)
                                }
                            },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            maxLines = 3
                        )

                        OutlinedTextField(
                            value = challenge.hint,
                            onValueChange = { newHint ->
                                questChallenges = questChallenges.toMutableList().apply {
                                    this[index] = this[index].copy(hint = newHint)
                                }
                            },
                            label = { Text("Hint") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            maxLines = 2
                        )

                        Text(
                            text = "Challenge Photo",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        PhotoUploader(
                            currentPhotoPath = challengePhotoPaths.getOrNull(index) ?: "",
                            onPhotoSelected = { path ->
                                challengePhotoPaths = challengePhotoPaths.toMutableList().apply {
                                    if (index >= size) {
                                        add(path)
                                    } else {
                                        set(index, path)
                                    }
                                }
                                coroutineScope.launch {
                                    val tempList = challengePhotoPaths.toMutableList()
                                    tempList[index] = ""
                                    delay(100)
                                    challengePhotoPaths = tempList
                                    tempList[index] = path
                                    challengePhotoPaths = tempList
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = challenge.latitude.toString(),
                                onValueChange = {},
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                readOnly = true
                            )

                            OutlinedTextField(
                                value = challenge.longitude.toString(),
                                onValueChange = {},
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                readOnly = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { showChallengeLocationPickerIndex = index },
                                modifier = Modifier.weight(1f).padding(end = 4.dp)
                            ) {
                                Text("Select on Map")
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val location = LocationManager.getLastLocation()
                                        location?.let {
                                            questChallenges = questChallenges.toMutableList().apply {
                                                this[index] = this[index].copy(
                                                    latitude = it.latitude,
                                                    longitude = it.longitude
                                                )
                                            }
                                        } ?: run {
                                            Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
                            ) {
                                Text("Use Current")
                            }
                        }

                        // Location picker para os challenges
                        if (showChallengeLocationPickerIndex >= 0) {
                            val challenge = questChallenges[showChallengeLocationPickerIndex]
                            val initialLocation = if (challenge.latitude != 0.0 && challenge.longitude != 0.0) {
                                LatLng(challenge.latitude, challenge.longitude)
                            } else {
                                null
                            }

                            LocationPickerDialog(
                                initialLocation = initialLocation,
                                onConfirm = { latLng ->
                                    questChallenges = questChallenges.toMutableList().apply {
                                        this[showChallengeLocationPickerIndex] = this[showChallengeLocationPickerIndex].copy(
                                            latitude = latLng.latitude,
                                            longitude = latLng.longitude
                                        )
                                    }
                                    showChallengeLocationPickerIndex = -1
                                },
                                onDismiss = { showChallengeLocationPickerIndex = -1 }
                            )
                        }
                    }
                }
            }

            // Botão para adicionar novo desafio
            Button(
                onClick = {
                    questChallenges = questChallenges + Challenge(
                        name = "",
                        description = "",
                        hintPhotoUrl = "",
                        latitude = 0.0,
                        longitude = 0.0,
                        orderInQuest = questChallenges.size + 1

                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Add Challenge")
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Botão para criar a quest
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (validateQuestForm(questName, questChallenges, questLatitude, questLongitude)) {
                            try{
                                val photoUrl = if (questPhotoPath.isNotEmpty()) {
                                    viewModel.uploadQuestPhoto(questPhotoPath)
                                } else ""

                                val updatedChallenges = questChallenges.mapIndexed { idx, challenge ->
                                    val path = challengePhotoPaths.getOrNull(idx) ?: ""
                                    if (path.isNotEmpty()) {
                                        val url = viewModel.uploadChallengePhoto(path)
                                        challenge.copy(hintPhotoUrl = url)
                                    } else {
                                        challenge
                                    }
                                }

                                viewModel.createQuest(
                                    Quest(
                                        name = questName,
                                        description = questDescription,
                                        photoUrl = photoUrl,
                                        startDate = questStartDate,
                                        endDate = questEndDate,
                                        latitude = questLatitude.toDoubleOrNull() ?: 0.0,
                                        longitude = questLongitude.toDoubleOrNull() ?: 0.0,
                                        isActive = true

                                    ),
                                    challenges = updatedChallenges
                                )

                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please fill all required fields",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState != QuestUiState.Loading
            ) {
                if (uiState == QuestUiState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Create Quest")
                }
            }
        }
    }
}

fun validateQuestForm(name: String, challenges: List<Challenge>, latitude: String, longitude: String): Boolean {
    if (name.isBlank()) return false
    if (challenges.isEmpty()) return false
    if (latitude.isBlank() || longitude.isBlank()) return false

    //challenges.forEach { challenge ->
    //    if (challenge.name.isBlank() || challenge.description.isBlank() || challenge.hint.isBlank() || challenge.hintPhotoUrl.isBlank() || challenge.latitude == 0.0 || challenge.longitude == 0.0) {
    //        return false
    //    }
    //}

    return true
}