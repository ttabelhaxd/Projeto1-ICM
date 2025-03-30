package com.example.snapquest.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.ui.components.ChallengeItem
import com.example.snapquest.ui.components.QuestDetailsSection
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quest?.name ?: "Quest Details") },
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
            quest?.let {
                val isQuestComplete = userQuest?.isQuestCompleted ?: false

                if (isQuestComplete) {
                    Text(
                        text = "Quest Completed!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                QuestDetailsSection(quest = it)
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
                    onCompleteClick = {
                        if (isUnlocked) {
                            user?.uid?.let { userId ->
                                viewModel.completeChallenge(userId, questId, challenge.id)
                            }
                        }
                    },
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