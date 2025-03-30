package com.example.snapquest.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.ui.components.Navbar
import com.example.snapquest.ui.components.ProgressItem
import com.example.snapquest.ui.components.TitleMessage
import com.example.snapquest.viewModels.HomeViewModel
import com.example.snapquest.viewModels.QuestViewModel
import com.google.firebase.firestore.firestoreSettings

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: HomeViewModel,
    questViewModel: QuestViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeUserQuests by questViewModel.userQuests.collectAsState()

    val ongoingQuest = activeUserQuests.firstOrNull { !it.isQuestCompleted }
    val quests by questViewModel.quests.collectAsState()
    val questName = quests.firstOrNull { it.id == ongoingQuest?.questId }?.name

    Scaffold(
        bottomBar = { Navbar(modifier = Modifier, navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleMessage(
                modifier = modifier,
                text1 = "Hello, ",
                text2 = viewModel.getName()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProgressItem(
                            title = "Quests",
                            count = currentUser?.questsCompleted ?: 0,
                            icon = Icons.Outlined.LocationSearching
                        )
                        ProgressItem(
                            title = "Challenges",
                            count = currentUser?.challengesCompleted ?: 0,
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ongoingQuest?.let { quest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("questDetails/${quest.questId}")
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Continue Your Quest",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You have an ongoing quest to complete!",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Quest Name: ${questName ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val completed = quest.completedChallenges.size
                        val total = questViewModel.quests.value.firstOrNull { it.id == quest.questId }?.participants?.size ?: 0

                        if (total > 0) {
                            Text(
                                text = "Progress: $completed/$total challenges",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}