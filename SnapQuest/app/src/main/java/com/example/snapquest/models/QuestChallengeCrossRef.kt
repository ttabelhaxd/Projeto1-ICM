package com.example.snapquest.models

import androidx.room.Entity

@Entity(primaryKeys = ["questId", "challengeId"])
data class QuestChallengeCrossRef(
    val questId: Int,
    val challengeId: Int,
    val completionStatus: Boolean = false,
    val completionOrder: Int = 0
)