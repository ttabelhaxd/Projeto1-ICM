package com.example.snapquest.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.snapquest.R
import com.example.snapquest.viewModels.QuestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestParticipantsScreen(
    questId: String,
    navController: NavHostController,
    viewModel: QuestViewModel
) {
    val quest by viewModel.getQuestById(questId).collectAsState(initial = null)
    val participants by viewModel.getParticipantDetails(quest?.participants ?: emptyList())
        .collectAsState(initial = emptyList())

    LaunchedEffect(quest) {
        Log.d("ParticipantsDebug", "Quest participants IDs: ${quest?.participants}")
    }

    LaunchedEffect(participants) {
        Log.d("ParticipantsDebug", "Loaded participants: ${participants.size}")
        participants.forEach {
            Log.d("ParticipantsDebug", "Participant: ${it.uid} - ${it.name}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Participants - ${quest?.name ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (participants.isEmpty()) {
                Text(
                    text = "No participants yet",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(participants) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Image(
                                painter = if (user.photoUrl.isNotEmpty()) {
                                    rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(user.photoUrl)
                                            .crossfade(true)
                                            .build()
                                    )
                                } else {
                                    painterResource(R.drawable.ic_profile_placeholder)
                                },
                                contentDescription = "User profile",
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "${user.name} (${user.email})",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}