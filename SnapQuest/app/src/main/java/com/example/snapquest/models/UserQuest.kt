package com.example.snapquest.models

import java.util.Date

data class UserQuest(
    val id: String = "",
    val userId: String = "",
    val questId: String = "",
    val completedChallenges: List<String> = emptyList(),
    val completedPhotos: Map<String, String> = emptyMap(),
    val isQuestCompleted: Boolean = false,
    val completedAt: Date? = null,
    val joinedAt: Date = Date()
)