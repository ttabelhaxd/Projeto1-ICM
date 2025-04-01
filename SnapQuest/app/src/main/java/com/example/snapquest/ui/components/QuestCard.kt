package com.example.snapquest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snapquest.models.Quest

@Composable
fun QuestCard(
    quest: Quest,
    isCreator: Boolean,
    isJoined: Boolean,
    isCompleted: Boolean,
    onJoinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewParticipantsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.padding(8.dp),
        border = if (isCompleted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (quest.photoUrl.isNotEmpty()) {
                QuestImageLoader(
                    imageUrl = quest.photoUrl,
                    contentDescription = "Quest image: ${quest.name}"
                )
            } else {
                QuestImageLoader(
                    imageUrl = "",
                    contentDescription = "Default quest image"
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(quest.name, style = MaterialTheme.typography.headlineSmall)
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Text(quest.description, modifier = Modifier.padding(vertical = 8.dp))

                if (isCompleted) {
                    Text(
                        text = "Quest completed!",
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (isCreator) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete Quest")
                        }

                        Button(
                            onClick = onViewParticipantsClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("Participants")
                        }
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Confirm Deletion") },
                            text = { Text("Are you sure you want to delete this quest?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        onDeleteClick()
                                        showDeleteDialog = false
                                    }
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDeleteDialog = false }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                } else {
                    Button(
                        onClick = onJoinClick,
                        enabled = !isJoined,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isJoined) "Joined" else "Join Quest")
                    }
                }
            }
        }
    }
}