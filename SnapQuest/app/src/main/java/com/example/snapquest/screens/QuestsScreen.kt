package com.example.snapquest.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.ui.components.Navbar
import com.example.snapquest.ui.components.QuestCard
import com.example.snapquest.viewModels.QuestUiState
import com.example.snapquest.viewModels.QuestViewModel

@Composable
fun QuestsScreen(
    navController: NavHostController,
    viewModel: QuestViewModel
) {
    val quests by viewModel.quests.collectAsState()
    val userQuests by viewModel.userQuests.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val joinedQuestIds = remember(userQuests) {
        userQuests.map { it.questId }.toSet()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchQuests()
        viewModel.fetchUserQuests()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is QuestUiState.QuestJoined -> {
                Toast.makeText(context, "Joined quest successfully", Toast.LENGTH_SHORT).show()
                QuestUiState.Idle
            }
            is QuestUiState.Error -> {
                Toast.makeText(context, (uiState as QuestUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createQuest") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Quest")
            }
        },
        bottomBar = { Navbar(modifier = Modifier, navController = navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                Text(
                    text = "Available Quests",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(quests) { quest ->
                QuestCard(
                    quest = quest,
                    isCreator = quest.creatorId == user?.uid,
                    isJoined = joinedQuestIds.contains(quest.id),
                    isCompleted = userQuests.find { it.questId == quest.id }?.completedChallenges?.size == quest.totalChallenges,
                    onJoinClick = {
                        if (user?.uid != null) {
                            viewModel.joinQuest(user!!.uid, quest.id)
                        }
                    },
                    onDeleteClick = {
                        viewModel.deleteQuest(quest.id)
                    },
                    onViewParticipantsClick = {
                        navController.navigate("participants/${quest.id}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (joinedQuestIds.contains(quest.id)) {
                                navController.navigate("questDetails/${quest.id}")
                            }
                        }
                )
            }
        }
    }
}