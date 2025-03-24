package com.example.snapquest.models

import androidx.room.Entity

@Entity(primaryKeys = ["userId", "questId"])
data class UserQuestCrossRef(
    val userId: Int,
    val questId: Int,
    val completionStatus: Boolean = false
)