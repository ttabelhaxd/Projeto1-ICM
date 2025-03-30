package com.example.snapquest.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.snapquest.R
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
    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer
        isUnlocked -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = when {
        isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
        isUnlocked -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = isUnlocked,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Imagem do desafio
            if (challenge.hintPhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(challenge.hintPhotoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Challenge image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    placeholder = painterResource(R.drawable.placeholder_challenge),
                    error = painterResource(R.drawable.placeholder_challenge)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone de status
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Status",
                    tint = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isUnlocked -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (challenge.description.isNotBlank()) {
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!isUnlocked) {
                        Text(
                            text = "Complete the previous challenge to unlock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Botão de completar
            if (isUnlocked && !isCompleted) {
                Button(
                    onClick = onCompleteClick,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp, end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Mark as Completed")
                }
            }
        }
    }
}