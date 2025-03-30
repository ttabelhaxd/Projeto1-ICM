package com.example.snapquest.models

import java.util.Date

data class UserQuest(
    val id: String = "",
    val userId: String = "",
    val questId: String = "",
    val completedChallenges: List<String> = emptyList(),
    val isQuestCompleted: Boolean = false,
    val joinedAt: Date = Date()
)