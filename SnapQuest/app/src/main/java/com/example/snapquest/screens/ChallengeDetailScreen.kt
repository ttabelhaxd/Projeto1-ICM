package com.example.snapquest.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.ui.components.QuestImageLoader
import com.example.snapquest.viewModels.QuestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailsScreen(
    questId: String,
    challengeId: String,
    navController: NavHostController,
    viewModel: QuestViewModel
) {
    val challenge by viewModel.getChallengeById(questId, challengeId).collectAsState(initial = null)
    val user by viewModel.currentUser.collectAsState()
    val userQuest by viewModel.getUserQuest(user?.uid ?: "", questId).collectAsState(initial = null)

    val isCompleted = userQuest?.completedChallenges?.contains(challengeId) ?: false
    val canComplete = user != null && !isCompleted

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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            challenge?.let { ch ->
                Text(
                    text = ch.name,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = ch.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (ch.hintPhotoUrl.isNotEmpty()) {
                    QuestImageLoader(
                        imageUrl = ch.hintPhotoUrl,
                        contentDescription = "Challenge hint image",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (ch.hint.isNotEmpty()) {
                    Text(
                        text = "Hint: ${ch.hint}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Mostrar coordenadas (opcional)
                Text(
                    text = "Location: ${ch.latitude}, ${ch.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (canComplete) {
                    Button(
                        onClick = {
                            user?.uid?.let { userId ->
                                viewModel.completeChallenge(userId, questId, challengeId)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Completed")
                    }
                } else if (isCompleted) {
                    Text(
                        text = "Completed!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}