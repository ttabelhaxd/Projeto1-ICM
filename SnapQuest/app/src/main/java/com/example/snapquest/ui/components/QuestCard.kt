package com.example.snapquest.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snapquest.models.Quest

@Composable
fun QuestCard(
    quest: Quest,
    onJoinClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = quest.name, fontWeight = FontWeight.Bold)
            Text(text = quest.description)
            Button(onClick = onJoinClick) {
                Text(text = "Join Quest")
            }
        }
    }
}