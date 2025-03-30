package com.example.snapquest.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snapquest.models.Challenge

@Composable
fun ChallengeItem(
    challenge: Challenge,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    onCompleteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = isUnlocked,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> MaterialTheme.colorScheme.primaryContainer
                isUnlocked -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = challenge.name,
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
                    isUnlocked -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (!isUnlocked) {
                Text(
                    text = "Complete the previous challenge to unlock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isUnlocked && !isCompleted) {
                Button(
                    onClick = onCompleteClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Complete")
                }
            }
        }
    }
}