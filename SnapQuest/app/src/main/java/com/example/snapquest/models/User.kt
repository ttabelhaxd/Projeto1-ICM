package com.example.snapquest.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var uid: String = "",
    var email: String = "",
    var name: String = "",
    var photoUrl: String = "",
    var questsCompleted: Int = 0,
    var challengesCompleted: Int = 0,
    var isAdmin: Boolean = false
)