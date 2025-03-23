package com.example.snapquest.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var uid: String = "",
    var email: String = "",
    var name: String = "",
    var photoUrl: String = "",
    var Quests: List<Quest> = listOf(),
    var QuestsCompleted: Int = 0,
    var ChallengesCompleted: Int = 0,
    var isAdmin: Boolean = false
)