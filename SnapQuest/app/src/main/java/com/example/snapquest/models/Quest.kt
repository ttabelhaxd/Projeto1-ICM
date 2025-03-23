package com.example.snapquest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var description: String = "",
    var photoUrl: String = "",
    var startDate: Date = Date(),
    var endDate: Date = Date(),
    var isActive: Boolean = false,
    var challenges: List<Challenge> = listOf(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var participants: List<User> = listOf(),
    var creator: User = User()
)