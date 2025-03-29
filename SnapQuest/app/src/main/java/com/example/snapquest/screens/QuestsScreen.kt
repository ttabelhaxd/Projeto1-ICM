package com.example.snapquest.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.ui.components.Navbar
import com.example.snapquest.ui.components.QuestCard
import com.example.snapquest.ui.components.TitleMessage
import com.example.snapquest.viewModels.QuestViewModel

@Composable
fun QuestsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: QuestViewModel
) {
    val quests by viewModel.quests.collectAsState()
    val user by viewModel.user.collectAsState()

    Scaffold(
        bottomBar = { Navbar(modifier = Modifier, navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TitleMessage(modifier = modifier, text1 = "", text2 = "Quests")
            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                items(quests) { quest ->
                    QuestCard(
                        quest = quest,
                        onJoinClick = {
                            viewModel.joinQuest(user, quest.id.toString())
                        }
                    )
                }
            }
        }
    }
}