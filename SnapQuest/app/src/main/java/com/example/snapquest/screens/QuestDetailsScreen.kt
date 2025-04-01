package com.example.snapquest.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.models.Quest
import com.example.snapquest.ui.components.*
import com.example.snapquest.utils.formatDate
import com.example.snapquest.viewModels.QuestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestDetailsScreen(
    questId: String,
    navController: NavHostController,
    viewModel: QuestViewModel
) {
    val quest by viewModel.getQuestById(questId).collectAsState(initial = null)
    val challenges by viewModel.getQuestChallenges(questId).collectAsState(initial = emptyList())
    val user by viewModel.currentUser.collectAsState()
    val userQuest by viewModel.getUserQuest(user?.uid ?: "", questId).collectAsState(initial = null)
    val isQuestComplete = userQuest?.completedChallenges?.size == challenges.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quest?.name ?: "Quest Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isQuestComplete) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (isQuestComplete) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            if (isQuestComplete) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quest Completed!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            quest?.let { currentQuest ->
                // Seção de status e datas
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    if (isQuestComplete) {
                        Text(
                            text = "Quest Completed",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    QuestDetailsSection(quest = currentQuest)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Datas formatadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Start Date",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = formatDate(currentQuest.startDate),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Column {
                            Text(
                                text = "End Date",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = formatDate(currentQuest.endDate),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Mapa com a localização
                if (currentQuest.latitude != 0.0 && currentQuest.longitude != 0.0) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LocationMap(
                        latitude = currentQuest.latitude,
                        longitude = currentQuest.longitude,
                        markerName = "Quest Location",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 16.dp)
                    )
                }

                Text(
                    text = "Challenges",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                challenges.sortedBy { it.orderInQuest }.forEach { challenge ->
                    val isUnlocked = remember(challenge.id, userQuest) {
                        val challengeIndex = challenges.indexOf(challenge)
                        challengeIndex == 0 || (userQuest?.completedChallenges?.contains(challenges[challengeIndex - 1].id) == true)
                    }

                    ChallengeItem(
                        challenge = challenge,
                        isCompleted = userQuest?.completedChallenges?.contains(challenge.id) ?: false,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked) {
                                navController.navigate("challengeDetails/${questId}/${challenge.id}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestDetailsSection(quest: Quest) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (quest.photoUrl.isNotEmpty()) {
            QuestImageLoader(
                imageUrl = quest.photoUrl,
                contentDescription = "Quest image: ${quest.name}",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            QuestImageLoader(
                imageUrl = "",
                contentDescription = "Default quest image",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = quest.name,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}