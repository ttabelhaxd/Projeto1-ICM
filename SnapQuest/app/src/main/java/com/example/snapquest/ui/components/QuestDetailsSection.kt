package com.example.snapquest.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snapquest.models.Quest

@Composable
fun QuestDetailsSection(quest: Quest) {
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

            QuestMetadata(quest)
        }
    }
}

@Composable
fun QuestMetadata(quest: Quest) {
    Text(
        text = "Início: ${quest.startDate}",
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Fim: ${quest.endDate}",
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Localização: ${quest.latitude}, ${quest.longitude}",
        style = MaterialTheme.typography.bodyMedium
    )
}
