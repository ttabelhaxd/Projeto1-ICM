package com.example.snapquest.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.example.snapquest.viewModels.QuestViewModel

@Composable
fun CreateQuestScreen(viewModel: QuestViewModel) {
    var questName by remember { mutableStateOf("") }
    var questDescription by remember { mutableStateOf("") }
    var questImageUrl by remember { mutableStateOf("") }
    var questStartDate by remember { mutableStateOf("") }
    var questEndDate by remember { mutableStateOf("") }
    var questLatitude by remember { mutableStateOf("") }
    var questLongitude by remember { mutableStateOf("") }
    var questChallenges by remember { mutableStateOf(listOf<Challenge>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Criar Nova Quest")

        BasicTextField(
            value = questName,
            onValueChange = { questName = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (questName.isEmpty()) {
                    Text("Nome da Quest")
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = questDescription,
            onValueChange = { questDescription = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (questDescription.isEmpty()) {
                    Text("Descrição da Quest")
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = questImageUrl,
            onValueChange = { questImageUrl = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (questImageUrl.isEmpty()) {
                    Text("URL da Imagem da Quest")
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = questStartDate,
            onValueChange = { questStartDate = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (questStartDate.isEmpty()) {
                    Text("Data de Início da Quest")
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = questEndDate,
            onValueChange = { questEndDate = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (questEndDate.isEmpty()) {
                    Text("Data de Término da Quest")
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = questLatitude,
            onValueChange = { questLatitude = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (questLatitude.isEmpty()) {
                    Text("Latitude da Quest")
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = questLongitude,
            onValueChange = { questLongitude = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (questLongitude.isEmpty()) {
                    Text("Longitude da Quest")
                }
                innerTextField()
            }
        )

        questChallenges.forEachIndexed { index, challenge ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Desafio ${index + 1}")

                BasicTextField(
                    value = challenge.name,
                    onValueChange = { newName ->
                        questChallenges = questChallenges.toMutableList().apply {
                            this[index] = this[index].copy(name = newName)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (challenge.name.isEmpty()) {
                            Text("Nome do Desafio")
                        }
                        innerTextField()
                    }
                )

                BasicTextField(
                    value = challenge.description,
                    onValueChange = { newDescription ->
                        questChallenges = questChallenges.toMutableList().apply {
                            this[index] = this[index].copy(description = newDescription)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (challenge.description.isEmpty()) {
                            Text("Descrição do Desafio")
                        }
                        innerTextField()
                    }
                )

                BasicTextField(
                    value = challenge.hintPhotoUrl,
                    onValueChange = { newHintPhotoUrl ->
                        questChallenges = questChallenges.toMutableList().apply {
                            this[index] = this[index].copy(hintPhotoUrl = newHintPhotoUrl)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (challenge.hintPhotoUrl.isEmpty()) {
                            Text("URL da Foto do Desafio")
                        }
                        innerTextField()
                    }
                )

                BasicTextField(
                    value = challenge.latitude.toString(),
                    onValueChange = { newLatitude ->
                        questChallenges = questChallenges.toMutableList().apply {
                            this[index] = this[index].copy(latitude = newLatitude.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (challenge.latitude == 0.0) {
                            Text("Latitude do Desafio")
                        }
                        innerTextField()
                    }
                )

                BasicTextField(
                    value = challenge.longitude.toString(),
                    onValueChange = { newLongitude ->
                        questChallenges = questChallenges.toMutableList().apply {
                            this[index] = this[index].copy(longitude = newLongitude.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (challenge.longitude == 0.0) {
                            Text("Longitude do Desafio")
                        }
                        innerTextField()
                    }
                )
            }
        }

        Button(onClick = {
            questChallenges = questChallenges + Challenge(
                name = "",
                description = "",
                hintPhotoUrl = "",
                latitude = 0.0,
                longitude = 0.0
            )
        }) {
            Text("Adicionar Desafio")
        }

        Button(onClick = {
            viewModel.createQuest(
                Quest(
                    name = questName,
                    description = questDescription,
                    photoUrl = questImageUrl,
                    startDate = questStartDate.toLong(),
                    endDate = questEndDate.toLong(),
                    latitude = questLatitude.toDouble(),
                    longitude = questLongitude.toDouble()
                ),
                challenges = questChallenges
            )
        }) {
            Text("Criar Quest")
        }
    }
}