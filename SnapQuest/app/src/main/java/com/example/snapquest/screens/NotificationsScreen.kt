package com.example.snapquest.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.snapquest.ui.components.Navbar
import com.example.snapquest.ui.components.NotificationItem
import com.example.snapquest.ui.components.TitleMessage
import com.example.snapquest.viewModels.HomeViewModel

@Composable
fun NotificationsScreen(
    modifier: Modifier,
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    val notifications by viewModel.notifications.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            viewModel.fetchNotifications(userId)
        }
    }

    Scaffold(
        bottomBar = { Navbar(modifier = Modifier, navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notifications yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            modifier = Modifier.padding(vertical = 8.dp),
                            onClick = {
                                currentUser?.uid?.let { userId ->
                                    viewModel.markNotificationAsRead(userId, notification.id)
                                }
                                when (notification.type) {
                                    "quest_completed" -> {
                                        navController.navigate("questDetails/${notification.relatedId}")
                                    }
                                    "quest_joined" -> {
                                        navController.navigate("questDetails/${notification.relatedId}")
                                    }
                                    "challenge_completed" -> {
                                        navController.navigate("challengeDetails/${notification.relatedId.split("_")[0]}/${notification.relatedId.split("_")[1]}")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}