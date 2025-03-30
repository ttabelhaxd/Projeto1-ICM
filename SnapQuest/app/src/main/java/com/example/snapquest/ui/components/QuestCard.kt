package com.example.snapquest.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snapquest.models.Quest

@Composable
fun QuestCard(
    quest: Quest,
    isCreator: Boolean,
    isJoined: Boolean,
    onJoinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(modifier = modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(quest.name, style = MaterialTheme.typography.headlineSmall)
            Text(quest.description, modifier = Modifier.padding(vertical = 8.dp))

            if (isCreator) {
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Quest")
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